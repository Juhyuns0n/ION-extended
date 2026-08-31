package capstone.voicereport.benchmark;

import org.springframework.http.HttpStatus;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class MixedWorkloadHttpDriver implements AutoCloseable {

    private static final int HTTP_CLIENT_THREADS = 20;
    private static final int REQUEST_TIMEOUT_SECONDS = 45;
    private static final int RESULT_TIMEOUT_SECONDS = 60;

    private final int port;
    private final long probeIntervalMs;
    private final ExecutorService voiceCallers;
    private final ExecutorService probeCallers;
    private final ExecutorService voiceHttpExecutor;
    private final ExecutorService probeHttpExecutor;
    private final HttpClient voiceClient;
    private final HttpClient probeClient;

    MixedWorkloadHttpDriver(int port, long probeIntervalMs, int maxRequests) {
        this.port = port;
        this.probeIntervalMs = probeIntervalMs;
        voiceCallers = Executors.newFixedThreadPool(maxRequests);
        probeCallers = Executors.newFixedThreadPool(maxRequests);
        voiceHttpExecutor = Executors.newFixedThreadPool(HTTP_CLIENT_THREADS);
        probeHttpExecutor = Executors.newFixedThreadPool(HTTP_CLIENT_THREADS);
        voiceClient = httpClient(voiceHttpExecutor);
        probeClient = httpClient(probeHttpExecutor);
    }

    ProbeResult runProbeOnly(int requests) throws Exception {
        CountDownLatch ready = new CountDownLatch(requests);
        CountDownLatch start = new CountDownLatch(1);
        AtomicReference<Long> releasedAt = new AtomicReference<>();
        List<Future<HttpSample>> futures = new ArrayList<>();

        for (int sequence = 0; sequence < requests; sequence++) {
            int requestSequence = sequence;
            futures.add(probeCallers.submit(() -> probe(
                    ready, start, releasedAt, requestSequence
            )));
        }

        assertTrue(ready.await(10, TimeUnit.SECONDS));
        long started = System.nanoTime();
        releasedAt.set(started);
        start.countDown();
        return summarizeProbe(collect(futures), started);
    }

    RunResult runMixed(
            String voicePath,
            int expectedVoiceStatus,
            int voiceRequests,
            int probeRequests
    ) throws Exception {
        CountDownLatch ready = new CountDownLatch(voiceRequests + probeRequests);
        CountDownLatch start = new CountDownLatch(1);
        AtomicReference<Long> releasedAt = new AtomicReference<>();
        List<Future<HttpSample>> voiceFutures = new ArrayList<>();
        List<Future<HttpSample>> probeFutures = new ArrayList<>();

        for (int i = 0; i < voiceRequests; i++) {
            voiceFutures.add(voiceCallers.submit(() -> voice(
                    voicePath, expectedVoiceStatus, ready, start
            )));
        }
        for (int sequence = 0; sequence < probeRequests; sequence++) {
            int requestSequence = sequence;
            probeFutures.add(probeCallers.submit(() -> probe(
                    ready, start, releasedAt, requestSequence
            )));
        }

        assertTrue(ready.await(10, TimeUnit.SECONDS));
        long started = System.nanoTime();
        releasedAt.set(started);
        start.countDown();

        List<HttpSample> voiceSamples = collect(voiceFutures);
        ProbeResult probe = summarizeProbe(collect(probeFutures), started);
        int voiceSuccesses = (int) voiceSamples.stream().filter(HttpSample::success).count();
        return new RunResult(started, probe, voiceRequests, voiceSuccesses);
    }

    private HttpSample voice(
            String path,
            int expectedStatus,
            CountDownLatch ready,
            CountDownLatch start
    ) throws InterruptedException {
        ready.countDown();
        start.await();
        long requestStarted = System.nanoTime();
        try {
            HttpResponse<Void> response = voiceClient.send(
                    voiceRequest(path), HttpResponse.BodyHandlers.discarding()
            );
            return sample(requestStarted, response.statusCode() == expectedStatus);
        } catch (Exception e) {
            return sample(requestStarted, false);
        }
    }

    private HttpSample probe(
            CountDownLatch ready,
            CountDownLatch start,
            AtomicReference<Long> releasedAt,
            int sequence
    ) throws InterruptedException {
        ready.countDown();
        start.await();
        waitUntil(releasedAt.get() + TimeUnit.MILLISECONDS.toNanos(sequence * probeIntervalMs));
        long requestStarted = System.nanoTime();
        try {
            HttpResponse<String> response = probeClient.send(
                    probeRequest(), HttpResponse.BodyHandlers.ofString()
            );
            boolean valid = response.statusCode() == HttpStatus.OK.value()
                    && response.body().contains("\"chapterId\":3");
            return sample(requestStarted, valid);
        } catch (Exception e) {
            return sample(requestStarted, false);
        }
    }

    private HttpSample sample(long requestStarted, boolean success) {
        return new HttpSample(elapsedMs(requestStarted), success, System.nanoTime());
    }

    private HttpRequest probeRequest() {
        return HttpRequest.newBuilder(URI.create(
                        "http://127.0.0.1:" + port + "/api/workbooks/chapter"
                ))
                .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
                .GET()
                .build();
    }

    private HttpRequest voiceRequest(String path) {
        String boundary = "ion-mixed-workload-boundary";
        byte[] body = ("--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"video\"; filename=\"sample.mp4\"\r\n"
                + "Content-Type: video/mp4\r\n\r\n"
                + "deterministic-test-media\r\n"
                + "--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8);
        return HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();
    }

    private static HttpClient httpClient(ExecutorService executor) {
        return HttpClient.newBuilder()
                .executor(executor)
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    private static List<HttpSample> collect(List<Future<HttpSample>> futures) throws Exception {
        List<HttpSample> samples = new ArrayList<>();
        for (Future<HttpSample> future : futures) {
            samples.add(future.get(RESULT_TIMEOUT_SECONDS, TimeUnit.SECONDS));
        }
        return samples;
    }

    private static ProbeResult summarizeProbe(List<HttpSample> samples, long releasedAt) {
        List<Double> successfulLatencies = samples.stream()
                .filter(HttpSample::success)
                .map(HttpSample::latencyMs)
                .sorted()
                .toList();
        int successes = successfulLatencies.size();
        long completedAt = samples.stream()
                .mapToLong(HttpSample::completedAtNanos)
                .max()
                .orElse(releasedAt);
        double responseWindowMs = (completedAt - releasedAt) / 1_000_000.0;
        return new ProbeResult(
                samples.size(),
                successes,
                samples.size() - successes,
                percentile(successfulLatencies, 0.50),
                percentile(successfulLatencies, 0.95),
                successfulLatencies.isEmpty() ? 0 : round(successfulLatencies.getLast()),
                round(responseWindowMs),
                round(successes / (responseWindowMs / 1_000.0))
        );
    }

    private static void waitUntil(long targetNanos) throws InterruptedException {
        while (true) {
            long remaining = targetNanos - System.nanoTime();
            if (remaining <= 0) return;
            LockSupport.parkNanos(remaining);
            if (Thread.interrupted()) throw new InterruptedException();
        }
    }

    static double elapsedMs(long startedNanos) {
        return (System.nanoTime() - startedNanos) / 1_000_000.0;
    }

    static double round(double value) {
        return Math.round(value * 1_000.0) / 1_000.0;
    }

    private static double percentile(List<Double> sortedValues, double quantile) {
        if (sortedValues.isEmpty()) return 0;
        int index = Math.max(0, (int) Math.ceil(sortedValues.size() * quantile) - 1);
        return round(sortedValues.get(index));
    }

    @Override
    public void close() {
        voiceCallers.shutdownNow();
        probeCallers.shutdownNow();
        voiceHttpExecutor.shutdownNow();
        probeHttpExecutor.shutdownNow();
    }

    record RunResult(
            long releasedAtNanos,
            ProbeResult probe,
            int voiceRequests,
            int voiceSuccesses
    ) {
    }

    record ProbeResult(
            int requests,
            int successes,
            int failures,
            double p50Ms,
            double p95Ms,
            double maxMs,
            double responseWindowMs,
            double throughputRps
    ) {
    }

    private record HttpSample(double latencyMs, boolean success, long completedAtNanos) {
    }
}
