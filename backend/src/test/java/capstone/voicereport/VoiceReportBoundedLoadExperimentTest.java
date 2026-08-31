package capstone.voicereport;

import capstone.voicereport.async.*;
import capstone.voicereport.controller.VoiceReportController;
import capstone.voicereport.dto.VoiceReportResponse;
import capstone.voicereport.entity.VoiceReport;
import capstone.voicereport.repository.VoiceReportRepository;
import capstone.voicereport.service.VoiceReportMapper;
import capstone.voicereport.service.VoiceReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@SpringBootTest(
        classes = VoiceReportBoundedLoadExperimentTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "server.tomcat.threads.max=10",
                "server.tomcat.threads.min-spare=10",
                "server.tomcat.accept-count=200",
                "server.tomcat.max-connections=200",
                "logging.level.root=WARN"
        }
)
class VoiceReportBoundedLoadExperimentTest {

    private static final int HTTP_THREADS = 10;
    private static final int WORKER_THREADS = 10;
    private static final long DOWNSTREAM_DELAY_MS = 2_000;
    private static final int WARMUP_BATCHES = 1;
    private static final int WARMUP_CONCURRENCY = 10;
    private static final List<Integer> CONCURRENCY_LEVELS = List.of(10, 50, 100);

    @LocalServerPort
    private int port;

    @org.springframework.beans.factory.annotation.Autowired
    private AsyncHarness asyncHarness;

    @Test
    @Timeout(150)
    void compareSynchronousAndAsynchronousBehaviorWithBoundedHttpThreads() throws Exception {
        ExecutorService clients = Executors.newFixedThreadPool(100);
        ExecutorService httpClientExecutor = Executors.newFixedThreadPool(20);
        HttpClient httpClient = HttpClient.newBuilder()
                .executor(httpClientExecutor)
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        List<Map<String, Object>> synchronousResults = new ArrayList<>();
        List<Map<String, Object>> asynchronousResults = new ArrayList<>();
        try {
            runHttpBatch(httpClient, clients, "/test/bounded/synchronous", WARMUP_CONCURRENCY, HttpStatus.OK.value());
            asyncHarness.beginRun(WARMUP_CONCURRENCY);
            runHttpBatch(httpClient, clients, "/api/voice-reports", WARMUP_CONCURRENCY, HttpStatus.ACCEPTED.value());
            assertTrue(asyncHarness.awaitCompletion(30, TimeUnit.SECONDS));

            for (int concurrency : CONCURRENCY_LEVELS) {
                BatchResult sync = runHttpBatch(
                        httpClient, clients, "/test/bounded/synchronous", concurrency, HttpStatus.OK.value()
                );
                synchronousResults.add(resultRow(sync, concurrency, sync.successes(), 0, null));

                asyncHarness.beginRun(concurrency);
                long completionStarted = System.nanoTime();
                BatchResult async = runHttpBatch(
                        httpClient, clients, "/api/voice-reports", concurrency, HttpStatus.ACCEPTED.value()
                );
                assertTrue(asyncHarness.awaitCompletion(40, TimeUnit.SECONDS));
                double totalCompletionMs = elapsedMs(completionStarted);
                asynchronousResults.add(resultRow(
                        async,
                        concurrency,
                        asyncHarness.completed(),
                        asyncHarness.failed(),
                        asyncHarness.peakQueueDepth(),
                        totalCompletionMs
                ));

                assertEquals(concurrency, sync.successes());
                assertEquals(concurrency, async.successes());
                assertEquals(concurrency, asyncHarness.completed());
                assertEquals(0, asyncHarness.failed());
                assertTrue(async.p95Ms() < DOWNSTREAM_DELAY_MS);
                if (concurrency > HTTP_THREADS) assertTrue(sync.p95Ms() > DOWNSTREAM_DELAY_MS);
            }
        } finally {
            clients.shutdownNow();
            httpClientExecutor.shutdownNow();
        }

        Map<String, Object> artifact = new LinkedHashMap<>();
        artifact.put("experiment", "bounded embedded HTTP load");
        artifact.put("server", "embedded Tomcat via SpringBootTest RANDOM_PORT");
        artifact.put("java_version", System.getProperty("java.version"));
        artifact.put("available_processors", Runtime.getRuntime().availableProcessors());
        artifact.put("http_request_threads", HTTP_THREADS);
        artifact.put("tomcat_accept_count", 200);
        artifact.put("tomcat_max_connections", 200);
        artifact.put("async_worker_threads", WORKER_THREADS);
        artifact.put("load_generator_threads", 100);
        artifact.put("http_client_executor_threads", 20);
        artifact.put("downstream_delay_ms", DOWNSTREAM_DELAY_MS);
        artifact.put("warmup_batches_per_variant", WARMUP_BATCHES);
        artifact.put("warmup_concurrency", WARMUP_CONCURRENCY);
        artifact.put("measured_batches_per_concurrency", 1);
        artifact.put("request_count_policy", "one request per concurrency slot");
        artifact.put("acceptance_throughput_definition", "successful HTTP responses divided by elapsed HTTP batch seconds");
        artifact.put("external_services", "none; persistence, object storage, queue transport, and analysis are local test doubles");
        artifact.put("synchronous", synchronousResults);
        artifact.put("asynchronous", asynchronousResults);

        Path output = outputPath();
        Files.createDirectories(output.getParent());
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(output.toFile(), artifact);
    }

    private BatchResult runHttpBatch(
            HttpClient client,
            ExecutorService clients,
            String path,
            int requests,
            int expectedStatus
    ) throws Exception {
        CountDownLatch ready = new CountDownLatch(requests);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<HttpSample>> futures = new ArrayList<>();
        long batchStarted = System.nanoTime();

        for (int i = 0; i < requests; i++) {
            futures.add(clients.submit(() -> {
                ready.countDown();
                start.await();
                long requestStarted = System.nanoTime();
                try {
                    HttpResponse<Void> response = client.send(request(path), HttpResponse.BodyHandlers.discarding());
                    return new HttpSample(elapsedMs(requestStarted), response.statusCode() == expectedStatus);
                } catch (Exception e) {
                    return new HttpSample(elapsedMs(requestStarted), false);
                }
            }));
        }

        assertTrue(ready.await(5, TimeUnit.SECONDS));
        batchStarted = System.nanoTime();
        start.countDown();
        List<HttpSample> samples = new ArrayList<>();
        for (Future<HttpSample> future : futures) samples.add(future.get(45, TimeUnit.SECONDS));
        double elapsedMs = elapsedMs(batchStarted);

        List<Double> successfulLatencies = samples.stream()
                .filter(HttpSample::success)
                .map(HttpSample::latencyMs)
                .sorted()
                .toList();
        int successes = successfulLatencies.size();
        return new BatchResult(
                samples.size(),
                successes,
                samples.size() - successes,
                percentile(successfulLatencies, 0.50),
                percentile(successfulLatencies, 0.95),
                successfulLatencies.isEmpty() ? 0 : round(successfulLatencies.getLast()),
                round(elapsedMs),
                round(successes / (elapsedMs / 1_000.0))
        );
    }

    private HttpRequest request(String path) {
        String boundary = "ion-bounded-load-boundary";
        byte[] body = ("--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"video\"; filename=\"sample.mp4\"\r\n"
                + "Content-Type: video/mp4\r\n\r\n"
                + "deterministic-test-media\r\n"
                + "--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8);
        return HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                .timeout(Duration.ofSeconds(45))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();
    }

    private Map<String, Object> resultRow(
            BatchResult batch,
            int concurrency,
            int completed,
            int processingFailures,
            Integer peakQueueDepth
    ) {
        return resultRow(batch, concurrency, completed, processingFailures, peakQueueDepth, batch.httpBatchMs());
    }

    private Map<String, Object> resultRow(
            BatchResult batch,
            int concurrency,
            int completed,
            int processingFailures,
            Integer peakQueueDepth,
            double totalCompletionMs
    ) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("concurrency", concurrency);
        row.put("requests", batch.requests());
        row.put("accepted_or_successful", batch.successes());
        row.put("request_failures", batch.failures());
        row.put("acceptance_p50_ms", batch.p50Ms());
        row.put("acceptance_p95_ms", batch.p95Ms());
        row.put("acceptance_max_ms", batch.maxMs());
        row.put("http_batch_ms", batch.httpBatchMs());
        row.put("acceptance_throughput_rps", batch.throughputRps());
        row.put("eventually_completed", completed);
        row.put("processing_failures", processingFailures);
        row.put("completion_rate", round(completed / (double) batch.requests()));
        row.put("total_completion_ms", round(totalCompletionMs));
        row.put("peak_worker_queue_depth", peakQueueDepth);
        return row;
    }

    private static Path outputPath() {
        String configured = System.getenv("VOICE_BOUNDED_LOAD_OUTPUT");
        return configured == null || configured.isBlank()
                ? Path.of("build", "async-voice", "bounded-load.json")
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

    private record HttpSample(double latencyMs, boolean success) {
    }

    private record BatchResult(
            int requests,
            int successes,
            int failures,
            double p50Ms,
            double p95Ms,
            double maxMs,
            double httpBatchMs,
            double throughputRps
    ) {
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            DataSourceTransactionManagerAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            SecurityAutoConfiguration.class,
            UserDetailsServiceAutoConfiguration.class,
            ManagementWebSecurityAutoConfiguration.class
    })
    static class TestApplication {

        @Bean
        AsyncHarness asyncHarness() {
            return new AsyncHarness();
        }

        @Bean
        VoiceReportService voiceReportService(AsyncHarness harness) {
            return harness.service();
        }

        @Bean
        VoiceReportController voiceReportController(VoiceReportService service) {
            return new VoiceReportController(service);
        }

        @Bean
        HistoricalSynchronousController historicalSynchronousController() {
            return new HistoricalSynchronousController();
        }

        @Bean
        Filter sessionUserFilter() {
            return new OncePerRequestFilter() {
                @Override
                protected void doFilterInternal(
                        jakarta.servlet.http.HttpServletRequest request,
                        jakarta.servlet.http.HttpServletResponse response,
                        jakarta.servlet.FilterChain filterChain
                ) throws jakarta.servlet.ServletException, IOException {
                    request.getSession(true).setAttribute("userId", 1);
                    filterChain.doFilter(request, response);
                }
            };
        }
    }

    @RestController
    static class HistoricalSynchronousController {
        @PostMapping(value = "/test/bounded/synchronous", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        ResponseEntity<Void> create(@RequestPart("video") MultipartFile media) throws InterruptedException {
            if (media.isEmpty()) return ResponseEntity.badRequest().build();
            Thread.sleep(DOWNSTREAM_DELAY_MS);
            return ResponseEntity.ok().build();
        }
    }

    static class AsyncHarness {
        private final AtomicInteger ids = new AtomicInteger();
        private final ConcurrentMap<Integer, JobRecord> jobs = new ConcurrentHashMap<>();
        private final InMemoryMediaStorage storage = new InMemoryMediaStorage();
        private final ThreadPoolExecutor workers = new ThreadPoolExecutor(
                WORKER_THREADS,
                WORKER_THREADS,
                0,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>()
        );
        private final AtomicInteger peakQueueDepth = new AtomicInteger();
        private final AtomicInteger completed = new AtomicInteger();
        private final AtomicInteger failed = new AtomicInteger();
        private final AtomicReference<CountDownLatch> completionLatch = new AtomicReference<>(new CountDownLatch(0));
        private final VoiceReportService service;

        AsyncHarness() {
            VoiceReportJobStateService state = mock(VoiceReportJobStateService.class);
            when(state.createPending(anyInt())).thenAnswer(invocation -> {
                int userId = invocation.getArgument(0);
                int reportId = ids.incrementAndGet();
                VoiceReport report = new VoiceReport();
                report.setReportId(reportId);
                report.setUserId(userId);
                report.setProcessingStatus(VoiceReportStatus.PENDING);
                jobs.put(reportId, new JobRecord(userId));
                return report;
            });
            doAnswer(invocation -> {
                jobs.get((int) invocation.getArgument(0)).mediaObjectKey = invocation.getArgument(1);
                return null;
            }).when(state).attachMediaObjectKey(anyInt(), any(String.class));
            when(state.claim(any())).thenAnswer(invocation -> {
                VoiceReportJobMessage message = invocation.getArgument(0);
                JobRecord job = jobs.get(message.reportId());
                synchronized (job) {
                    if (job.status == VoiceReportStatus.PENDING) {
                        job.status = VoiceReportStatus.PROCESSING;
                        return VoiceReportClaim.CLAIMED;
                    }
                    return job.status == VoiceReportStatus.PROCESSING
                            ? VoiceReportClaim.ACTIVE
                            : VoiceReportClaim.TERMINAL;
                }
            });
            when(state.getContext(anyInt())).thenAnswer(invocation -> {
                JobRecord job = jobs.get((int) invocation.getArgument(0));
                return new VoiceReportJobContext(job.userId, job.mediaObjectKey);
            });
            doAnswer(invocation -> {
                JobRecord job = jobs.get((int) invocation.getArgument(0));
                job.status = VoiceReportStatus.COMPLETED;
                completed.incrementAndGet();
                return null;
            }).when(state).complete(anyInt(), any(VoiceReportResponse.class));
            doAnswer(invocation -> {
                JobRecord job = jobs.get((int) invocation.getArgument(0));
                job.status = VoiceReportStatus.FAILED;
                failed.incrementAndGet();
                return null;
            }).when(state).fail(anyInt(), any(String.class));
            doAnswer(invocation -> {
                JobRecord job = jobs.get((int) invocation.getArgument(0));
                job.status = VoiceReportStatus.FAILED;
                failed.incrementAndGet();
                return null;
            }).when(state).failPendingSubmission(anyInt(), any(String.class));

            VoiceReportAnalysisProcessor analysis = (sourceMedia, sourceName, userId, reportId) -> {
                try {
                    Thread.sleep(DOWNSTREAM_DELAY_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted fake processing", e);
                }
                VoiceReportResponse response = new VoiceReportResponse();
                response.setReportId(reportId);
                return response;
            };
            VoiceReportJobProcessor processor = new VoiceReportJobProcessor(state, storage, analysis);
            VoiceReportJobPublisher publisher = message -> {
                CountDownLatch runLatch = completionLatch.get();
                workers.execute(() -> {
                    processor.process(message);
                    runLatch.countDown();
                });
                peakQueueDepth.accumulateAndGet(workers.getQueue().size(), Math::max);
            };
            service = new VoiceReportService(
                    mock(VoiceReportRepository.class),
                    state,
                    storage,
                    publisher,
                    mock(VoiceReportMapper.class)
            );
        }

        VoiceReportService service() {
            return service;
        }

        void beginRun(int expectedJobs) {
            assertEquals(0, workers.getQueue().size());
            completed.set(0);
            failed.set(0);
            peakQueueDepth.set(0);
            completionLatch.set(new CountDownLatch(expectedJobs));
        }

        boolean awaitCompletion(long timeout, TimeUnit unit) throws InterruptedException {
            return completionLatch.get().await(timeout, unit);
        }

        int completed() {
            return completed.get();
        }

        int failed() {
            return failed.get();
        }

        int peakQueueDepth() {
            return peakQueueDepth.get();
        }

        @PreDestroy
        void shutdown() {
            workers.shutdownNow();
        }
    }

    static class InMemoryMediaStorage implements VoiceReportMediaStorage {
        private final ConcurrentMap<String, byte[]> objects = new ConcurrentHashMap<>();

        @Override
        public String store(
                int reportId,
                String originalFilename,
                java.io.InputStream data,
                long contentLength,
                String contentType
        ) {
            String key = "voice-reports/" + reportId + "/input.mp4";
            try {
                objects.put(key, data.readAllBytes());
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            return key;
        }

        @Override
        public byte[] load(String mediaObjectKey) {
            byte[] value = objects.get(mediaObjectKey);
            if (value == null) throw new IllegalStateException("Missing in-memory media");
            return value;
        }

        @Override
        public void delete(String mediaObjectKey) {
            objects.remove(mediaObjectKey);
        }
    }

    static class JobRecord {
        private final int userId;
        private volatile String mediaObjectKey;
        private volatile VoiceReportStatus status = VoiceReportStatus.PENDING;

        JobRecord(int userId) {
            this.userId = userId;
        }
    }
}
