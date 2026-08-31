package capstone.voicereport;

import capstone.voicereport.async.VoiceReportStatus;
import capstone.voicereport.controller.VoiceReportController;
import capstone.voicereport.dto.VoiceReportSubmissionResponse;
import capstone.voicereport.service.VoiceReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

class VoiceReportAsyncAcceptanceBenchmarkTest {

    private static final long DOWNSTREAM_DELAY_MS = 2_000;
    private static final int WARMUP_BATCHES = 1;
    private static final int MEASURED_BATCHES = 3;
    private static final List<Integer> CONCURRENCY_LEVELS = List.of(1, 10, 50);

    @Test
    @Timeout(60)
    void recordAcceptedLatencySeparatelyFromDeterministicBackgroundCompletion() throws Exception {
        ExecutorService backgroundWorkers = Executors.newFixedThreadPool(50);
        List<CompletableFuture<Void>> submittedJobs = new CopyOnWriteArrayList<>();
        AtomicInteger reportIds = new AtomicInteger();
        AtomicInteger completedJobs = new AtomicInteger();
        VoiceReportService service = mock(VoiceReportService.class);
        when(service.submit(anyInt(), any())).thenAnswer(invocation -> {
            int reportId = reportIds.incrementAndGet();
            submittedJobs.add(CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(DOWNSTREAM_DELAY_MS);
                    completedJobs.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new CompletionException(e);
                }
            }, backgroundWorkers));
            return new VoiceReportSubmissionResponse(reportId, VoiceReportStatus.PENDING);
        });

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new VoiceReportController(service)).build();
        List<Map<String, Object>> results = new ArrayList<>();

        try {
            for (int concurrency : CONCURRENCY_LEVELS) {
                for (int i = 0; i < WARMUP_BATCHES; i++) {
                    int start = submittedJobs.size();
                    runAcceptanceBatch(mockMvc, concurrency);
                    awaitJobs(submittedJobs.subList(start, submittedJobs.size()));
                }

                List<Sample> samples = new ArrayList<>();
                int completedBefore = completedJobs.get();
                long completionStarted = System.nanoTime();
                for (int i = 0; i < MEASURED_BATCHES; i++) {
                    int start = submittedJobs.size();
                    samples.addAll(runAcceptanceBatch(mockMvc, concurrency));
                    awaitJobs(submittedJobs.subList(start, submittedJobs.size()));
                }
                double completionMs = elapsedMs(completionStarted);

                List<Double> acceptedLatencies = samples.stream()
                        .filter(Sample::accepted)
                        .map(Sample::latencyMs)
                        .sorted()
                        .toList();
                long accepted = acceptedLatencies.size();
                long failures = samples.size() - accepted;
                int eventuallyCompleted = completedJobs.get() - completedBefore;

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("concurrency", concurrency);
                row.put("requests", samples.size());
                row.put("accepted", accepted);
                row.put("acceptance_failures", failures);
                row.put("acceptance_p50_ms", percentile(acceptedLatencies, 0.50));
                row.put("acceptance_p95_ms", percentile(acceptedLatencies, 0.95));
                row.put("eventually_completed", eventuallyCompleted);
                row.put("processing_failures", samples.size() - eventuallyCompleted);
                row.put("measured_batches_completion_ms", round(completionMs));
                results.add(row);

                assertEquals(0, failures);
                assertEquals(samples.size(), eventuallyCompleted);
            }
        } finally {
            backgroundWorkers.shutdownNow();
        }

        Map<String, Object> artifact = new LinkedHashMap<>();
        artifact.put("phase", "async");
        artifact.put("benchmark_kind", "Spring MockMvc async acceptance boundary with in-memory fake worker");
        artifact.put("simulated_downstream_delay_ms", DOWNSTREAM_DELAY_MS);
        artifact.put("warmup_batches", WARMUP_BATCHES);
        artifact.put("measured_batches", MEASURED_BATCHES);
        artifact.put("external_services", "none");
        artifact.put("interpretation", "HTTP request acceptance is decoupled from downstream processing duration");
        artifact.put("results", results);

        Path output = outputPath();
        Files.createDirectories(output.getParent());
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(output.toFile(), artifact);
    }

    private static List<Sample> runAcceptanceBatch(MockMvc mockMvc, int concurrency) throws Exception {
        ExecutorService requests = Executors.newFixedThreadPool(concurrency);
        CountDownLatch ready = new CountDownLatch(concurrency);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Sample>> futures = new ArrayList<>();
        try {
            for (int i = 0; i < concurrency; i++) {
                futures.add(requests.submit(() -> {
                    ready.countDown();
                    start.await();
                    long started = System.nanoTime();
                    try {
                        int status = mockMvc.perform(multipart("/api/voice-reports")
                                        .file(new MockMultipartFile(
                                                "video", "sample.mp4", "video/mp4", new byte[]{1, 2, 3}
                                        ))
                                        .sessionAttr("userId", 1))
                                .andReturn().getResponse().getStatus();
                        return new Sample(elapsedMs(started), status == HttpStatus.ACCEPTED.value());
                    } catch (Exception e) {
                        return new Sample(elapsedMs(started), false);
                    }
                }));
            }
            ready.await(5, TimeUnit.SECONDS);
            start.countDown();
            List<Sample> samples = new ArrayList<>();
            for (Future<Sample> future : futures) samples.add(future.get(10, TimeUnit.SECONDS));
            return samples;
        } finally {
            requests.shutdownNow();
        }
    }

    private static void awaitJobs(List<CompletableFuture<Void>> jobs) {
        CompletableFuture.allOf(jobs.toArray(CompletableFuture[]::new)).join();
    }

    private static Path outputPath() {
        String configured = System.getenv("VOICE_ASYNC_OUTPUT");
        return configured == null || configured.isBlank()
                ? Path.of("build", "async-voice", "async.json")
                : Path.of(configured);
    }

    private static double percentile(List<Double> sortedValues, double quantile) {
        if (sortedValues.isEmpty()) return 0;
        int index = Math.max(0, (int) Math.ceil(sortedValues.size() * quantile) - 1);
        return round(sortedValues.get(index));
    }

    private static double elapsedMs(long startedNanos) {
        return (System.nanoTime() - startedNanos) / 1_000_000.0;
    }

    private static double round(double value) {
        return Math.round(value * 1_000.0) / 1_000.0;
    }

    private record Sample(double latencyMs, boolean accepted) {
    }
}
