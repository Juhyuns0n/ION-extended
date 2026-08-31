package capstone.voicereport;

import capstone.voicereport.dto.VoiceReportResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

class VoiceReportSynchronousBaselineBenchmarkTest {

    private static final long DOWNSTREAM_DELAY_MS = 2_000;
    private static final int WARMUP_BATCHES = 1;
    private static final int MEASURED_BATCHES = 3;
    private static final List<Integer> CONCURRENCY_LEVELS = List.of(1, 10, 50);

    @Test
    @Timeout(60)
    void recordSynchronousHttpLatencyWithDeterministicDownstreamDelay() throws Exception {
        AtomicInteger reportIds = new AtomicInteger();
        HistoricalSynchronousHandler handler = (userId, media) -> {
            Thread.sleep(DOWNSTREAM_DELAY_MS);
            VoiceReportResponse response = new VoiceReportResponse();
            response.setReportId(reportIds.incrementAndGet());
            return response;
        };

        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new HistoricalSynchronousController(handler))
                .build();

        List<Map<String, Object>> results = new ArrayList<>();
        for (int concurrency : CONCURRENCY_LEVELS) {
            for (int i = 0; i < WARMUP_BATCHES; i++) {
                runBatch(mockMvc, concurrency);
            }

            List<Sample> samples = new ArrayList<>();
            long completionStart = System.nanoTime();
            for (int i = 0; i < MEASURED_BATCHES; i++) {
                samples.addAll(runBatch(mockMvc, concurrency));
            }
            double completionMs = elapsedMs(completionStart);

            List<Double> successfulLatencies = samples.stream()
                    .filter(Sample::success)
                    .map(Sample::latencyMs)
                    .sorted()
                    .toList();
            long successes = successfulLatencies.size();
            long failures = samples.size() - successes;

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("concurrency", concurrency);
            row.put("requests", samples.size());
            row.put("successes", successes);
            row.put("failures", failures);
            row.put("http_response_p50_ms", percentile(successfulLatencies, 0.50));
            row.put("http_response_p95_ms", percentile(successfulLatencies, 0.95));
            row.put("measured_batches_completion_ms", round(completionMs));
            results.add(row);

            assertEquals(0, failures);
            assertTrue(percentile(successfulLatencies, 0.50) >= DOWNSTREAM_DELAY_MS * 0.95);
        }

        Map<String, Object> artifact = new LinkedHashMap<>();
        artifact.put("phase", "baseline");
        artifact.put("benchmark_kind", "Spring MockMvc synchronous HTTP boundary");
        artifact.put("simulated_downstream_delay_ms", DOWNSTREAM_DELAY_MS);
        artifact.put("warmup_batches", WARMUP_BATCHES);
        artifact.put("measured_batches", MEASURED_BATCHES);
        artifact.put("external_services", "none");
        artifact.put("interpretation", "HTTP response remains coupled to downstream processing duration");
        artifact.put("results", results);

        Path output = outputPath();
        Files.createDirectories(output.getParent());
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(output.toFile(), artifact);
    }

    private static List<Sample> runBatch(MockMvc mockMvc, int concurrency) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(concurrency);
        CountDownLatch ready = new CountDownLatch(concurrency);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Sample>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < concurrency; i++) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    long started = System.nanoTime();
                    int status;
                    try {
                        status = mockMvc.perform(
                                        multipart("/api/voice-reports")
                                                .file(new MockMultipartFile(
                                                        "video",
                                                        "sample.mp4",
                                                        "video/mp4",
                                                        new byte[]{1, 2, 3}
                                                ))
                                                .sessionAttr("userId", 1)
                                )
                                .andReturn()
                                .getResponse()
                                .getStatus();
                    } catch (Exception e) {
                        return new Sample(elapsedMs(started), false);
                    }
                    return new Sample(
                            elapsedMs(started),
                            status == HttpStatus.CREATED.value()
                    );
                }));
            }

            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();

            List<Sample> samples = new ArrayList<>();
            for (Future<Sample> future : futures) {
                samples.add(future.get(10, TimeUnit.SECONDS));
            }
            return samples;
        } finally {
            executor.shutdownNow();
        }
    }

    private static Path outputPath() {
        String configured = System.getenv("VOICE_BASELINE_OUTPUT");
        if (configured == null || configured.isBlank()) {
            return Path.of("build", "async-voice", "baseline.json");
        }
        return Path.of(configured);
    }

    private static double percentile(List<Double> sortedValues, double quantile) {
        if (sortedValues.isEmpty()) {
            return 0;
        }
        int index = Math.max(0, (int) Math.ceil(sortedValues.size() * quantile) - 1);
        return round(sortedValues.get(index));
    }

    private static double elapsedMs(long startedNanos) {
        return (System.nanoTime() - startedNanos) / 1_000_000.0;
    }

    private static double round(double value) {
        return Math.round(value * 1_000.0) / 1_000.0;
    }

    private record Sample(double latencyMs, boolean success) {
    }

    @FunctionalInterface
    private interface HistoricalSynchronousHandler {
        VoiceReportResponse create(int userId, MultipartFile media) throws InterruptedException;
    }

    @RestController
    private static class HistoricalSynchronousController {
        private final HistoricalSynchronousHandler handler;

        private HistoricalSynchronousController(HistoricalSynchronousHandler handler) {
            this.handler = handler;
        }

        @PostMapping(value = "/api/voice-reports", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        ResponseEntity<VoiceReportResponse> create(
                @RequestPart("video") MultipartFile media,
                HttpSession session
        ) throws InterruptedException {
            int userId = (Integer) session.getAttribute("userId");
            return ResponseEntity.status(HttpStatus.CREATED).body(handler.create(userId, media));
        }
    }
}
