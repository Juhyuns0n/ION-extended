package capstone.voicereport.benchmark;

import capstone.home.repository.UserProfileRepository;
import capstone.user.entity.User;
import capstone.user.repository.UserRepository;
import capstone.voicereport.controller.VoiceReportController;
import capstone.voicereport.service.VoiceReportService;
import capstone.workbook.controller.WorkbookController;
import capstone.workbook.repository.SimulationRepository;
import capstone.workbook.repository.WorkbookRepository;
import capstone.workbook.repository.WorkbookTheoryRepository;
import capstone.workbook.service.WorkbookPythonClient;
import capstone.workbook.service.WorkbookService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(
        classes = VoiceReportMixedWorkloadExperimentTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "server.tomcat.threads.max=10",
                "server.tomcat.threads.min-spare=10",
                "server.tomcat.accept-count=200",
                "server.tomcat.max-connections=200",
                "logging.level.root=WARN"
        }
)
class VoiceReportMixedWorkloadExperimentTest {

    private static final int HTTP_THREADS = 10;
    private static final int WORKER_THREADS = 10;
    private static final long DOWNSTREAM_DELAY_MS = 2_000;
    private static final int PROBE_REQUESTS = 100;
    private static final long PROBE_INTERVAL_MS = 50;
    private static final double PROBE_OFFERED_RATE_RPS = 1_000.0 / PROBE_INTERVAL_MS;
    private static final int WARMUP_PROBES = 20;
    private static final int WARMUP_VOICE_CONCURRENCY = 10;
    private static final List<Integer> VOICE_CONCURRENCY_LEVELS = List.of(50, 100);

    @LocalServerPort
    private int port;

    @org.springframework.beans.factory.annotation.Autowired
    private MixedVoiceLoadHarness asyncHarness;

    @org.springframework.beans.factory.annotation.Autowired
    private MixedVoiceLoadHarness.SynchronousController synchronousController;

    @Test
    @Timeout(180)
    void measureWorkbookIsolationFromSynchronousAndAsynchronousVoiceLoad() throws Exception {
        try (MixedWorkloadHttpDriver driver = new MixedWorkloadHttpDriver(
                port, PROBE_INTERVAL_MS, PROBE_REQUESTS
        )) {
            warmUp(driver);

            MixedWorkloadHttpDriver.ProbeResult baseline = driver.runProbeOnly(PROBE_REQUESTS);
            assertEquals(PROBE_REQUESTS, baseline.successes());
            assertEquals(0, baseline.failures());

            List<ConditionResult> conditions = new ArrayList<>();
            for (int voiceConcurrency : VOICE_CONCURRENCY_LEVELS) {
                ConditionResult synchronous = runMixed(
                        driver, VoiceMode.SYNCHRONOUS, voiceConcurrency, PROBE_REQUESTS
                );
                ConditionResult asynchronous = runMixed(
                        driver, VoiceMode.ASYNCHRONOUS, voiceConcurrency, PROBE_REQUESTS
                );
                conditions.add(synchronous);
                conditions.add(asynchronous);

                assertSuccessful(synchronous, voiceConcurrency);
                assertSuccessful(asynchronous, voiceConcurrency);
                assertTrue(synchronous.probe().p95Ms() > DOWNSTREAM_DELAY_MS);
                assertTrue(asynchronous.probe().p95Ms() < DOWNSTREAM_DELAY_MS);
                assertTrue(synchronous.probe().p95Ms() > asynchronous.probe().p95Ms());
            }

            writeArtifact(baseline, conditions);
        }
    }

    private void warmUp(MixedWorkloadHttpDriver driver) throws Exception {
        driver.runProbeOnly(WARMUP_PROBES);
        runMixed(driver, VoiceMode.SYNCHRONOUS, WARMUP_VOICE_CONCURRENCY, WARMUP_PROBES);
        runMixed(driver, VoiceMode.ASYNCHRONOUS, WARMUP_VOICE_CONCURRENCY, WARMUP_PROBES);
    }

    private ConditionResult runMixed(
            MixedWorkloadHttpDriver driver,
            VoiceMode mode,
            int voiceRequests,
            int probeRequests
    ) throws Exception {
        if (mode == VoiceMode.SYNCHRONOUS) {
            synchronousController.beginRun();
        } else {
            asyncHarness.beginRun(voiceRequests);
        }

        MixedWorkloadHttpDriver.RunResult http = driver.runMixed(
                mode.path,
                mode.expectedStatus,
                voiceRequests,
                probeRequests
        );
        if (mode == VoiceMode.ASYNCHRONOUS) {
            assertTrue(asyncHarness.awaitCompletion(45, TimeUnit.SECONDS));
        }

        VoiceResult voice = new VoiceResult(
                http.voiceRequests(),
                http.voiceSuccesses(),
                http.voiceRequests() - http.voiceSuccesses(),
                mode == VoiceMode.SYNCHRONOUS ? http.voiceSuccesses() : asyncHarness.completed(),
                mode == VoiceMode.SYNCHRONOUS ? 0 : asyncHarness.failed(),
                MixedWorkloadHttpDriver.round(
                        MixedWorkloadHttpDriver.elapsedMs(http.releasedAtNanos())
                ),
                mode == VoiceMode.SYNCHRONOUS ? null : asyncHarness.peakQueueDepth(),
                mode == VoiceMode.SYNCHRONOUS ? synchronousController.peakActive() : null
        );
        return new ConditionResult(mode, voiceRequests, http.probe(), voice);
    }

    private void writeArtifact(
            MixedWorkloadHttpDriver.ProbeResult baseline,
            List<ConditionResult> conditions
    ) throws IOException {
        Map<String, Object> server = new LinkedHashMap<>();
        server.put("type", "embedded Tomcat via SpringBootTest RANDOM_PORT");
        server.put("java_version", System.getProperty("java.version"));
        server.put("available_processors", Runtime.getRuntime().availableProcessors());
        server.put("http_request_threads", HTTP_THREADS);
        server.put("tomcat_accept_count", 200);
        server.put("tomcat_max_connections", 200);
        server.put("async_worker_threads", WORKER_THREADS);

        Map<String, Object> probeWorkload = new LinkedHashMap<>();
        probeWorkload.put("endpoint", "GET /api/workbooks/chapter");
        probeWorkload.put("repository", "deterministic mock");
        probeWorkload.put("requests_per_condition", PROBE_REQUESTS);
        probeWorkload.put("release_interval_ms", PROBE_INTERVAL_MS);
        probeWorkload.put("offered_rate_rps", PROBE_OFFERED_RATE_RPS);
        probeWorkload.put("common_start_with_voice", true);

        Map<String, Object> artifact = new LinkedHashMap<>();
        artifact.put("experiment", "mixed-workload isolation");
        artifact.put("server", server);
        artifact.put("voice_workload", Map.of(
                "downstream_delay_ms", DOWNSTREAM_DELAY_MS,
                "concurrency_levels", VOICE_CONCURRENCY_LEVELS
        ));
        artifact.put("warmup", Map.of(
                "runs_per_condition", 1,
                "probe_requests", WARMUP_PROBES,
                "voice_concurrency", WARMUP_VOICE_CONCURRENCY
        ));
        artifact.put("probe_workload", probeWorkload);
        artifact.put("external_services_used", List.of());
        artifact.put("probe_only", probeMap(baseline));
        artifact.put("mixed_conditions", conditions.stream().map(this::conditionMap).toList());

        Path output = outputPath();
        Files.createDirectories(output.getParent());
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(output.toFile(), artifact);
    }

    private Map<String, Object> conditionMap(ConditionResult condition) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("voice_mode", condition.mode().artifactName);
        row.put("voice_concurrency", condition.voiceConcurrency());
        row.put("probe", probeMap(condition.probe()));
        row.put("voice", voiceMap(condition.voice()));
        return row;
    }

    private static Map<String, Object> probeMap(MixedWorkloadHttpDriver.ProbeResult result) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("requests", result.requests());
        map.put("successful_responses", result.successes());
        map.put("failures_or_timeouts", result.failures());
        map.put("p50_ms", result.p50Ms());
        map.put("p95_ms", result.p95Ms());
        map.put("max_ms", result.maxMs());
        map.put("response_window_ms", result.responseWindowMs());
        map.put("response_throughput_rps", result.throughputRps());
        return map;
    }

    private static Map<String, Object> voiceMap(VoiceResult result) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("submissions", result.submissions());
        map.put("accepted_or_successful", result.acceptedOrSuccessful());
        map.put("request_failures", result.requestFailures());
        map.put("eventually_completed", result.eventuallyCompleted());
        map.put("failed_jobs", result.failedJobs());
        map.put("total_completion_ms", result.totalCompletionMs());
        map.put("peak_async_worker_queue_depth", result.peakWorkerQueueDepth());
        map.put(
                "peak_synchronous_voice_requests_holding_tomcat_threads",
                result.peakSynchronousActive()
        );
        return map;
    }

    private static void assertSuccessful(ConditionResult result, int expectedVoiceRequests) {
        assertEquals(PROBE_REQUESTS, result.probe().successes());
        assertEquals(0, result.probe().failures());
        assertEquals(expectedVoiceRequests, result.voice().acceptedOrSuccessful());
        assertEquals(0, result.voice().requestFailures());
        assertEquals(expectedVoiceRequests, result.voice().eventuallyCompleted());
        assertEquals(0, result.voice().failedJobs());
    }

    private static Path outputPath() {
        String configured = System.getenv("VOICE_MIXED_LOAD_OUTPUT");
        return configured == null || configured.isBlank()
                ? Path.of("build", "async-voice", "mixed-workload.json")
                : Path.of(configured);
    }

    private enum VoiceMode {
        SYNCHRONOUS(
                "synchronous",
                "/test/mixed/synchronous-voice",
                HttpStatus.OK.value()
        ),
        ASYNCHRONOUS(
                "asynchronous",
                "/api/voice-reports",
                HttpStatus.ACCEPTED.value()
        );

        private final String artifactName;
        private final String path;
        private final int expectedStatus;

        VoiceMode(String artifactName, String path, int expectedStatus) {
            this.artifactName = artifactName;
            this.path = path;
            this.expectedStatus = expectedStatus;
        }
    }

    private record VoiceResult(
            int submissions,
            int acceptedOrSuccessful,
            int requestFailures,
            int eventuallyCompleted,
            int failedJobs,
            double totalCompletionMs,
            Integer peakWorkerQueueDepth,
            Integer peakSynchronousActive
    ) {
    }

    private record ConditionResult(
            VoiceMode mode,
            int voiceConcurrency,
            MixedWorkloadHttpDriver.ProbeResult probe,
            VoiceResult voice
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
        MixedVoiceLoadHarness mixedVoiceLoadHarness() {
            return new MixedVoiceLoadHarness(WORKER_THREADS, DOWNSTREAM_DELAY_MS);
        }

        @Bean
        VoiceReportService voiceReportService(MixedVoiceLoadHarness harness) {
            return harness.service();
        }

        @Bean
        VoiceReportController voiceReportController(VoiceReportService service) {
            return new VoiceReportController(service);
        }

        @Bean
        MixedVoiceLoadHarness.SynchronousController historicalSynchronousController() {
            return new MixedVoiceLoadHarness.SynchronousController(DOWNSTREAM_DELAY_MS);
        }

        @Bean
        WorkbookService workbookService(ObjectMapper objectMapper) {
            UserRepository userRepository = mock(UserRepository.class);
            User user = User.builder().userId(1).nowChapter(3).build();
            when(userRepository.findById(1)).thenReturn(Optional.of(user));
            return new WorkbookService(
                    objectMapper,
                    mock(WorkbookPythonClient.class),
                    mock(WorkbookRepository.class),
                    mock(SimulationRepository.class),
                    userRepository,
                    mock(UserProfileRepository.class),
                    mock(WorkbookTheoryRepository.class)
            );
        }

        @Bean
        WorkbookController workbookController(WorkbookService service) {
            return new WorkbookController(service);
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
}
