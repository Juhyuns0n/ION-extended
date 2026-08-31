package capstone.voicereport.benchmark;

import capstone.voicereport.async.*;
import capstone.voicereport.dto.VoiceReportResponse;
import capstone.voicereport.entity.VoiceReport;
import capstone.voicereport.repository.VoiceReportRepository;
import capstone.voicereport.service.VoiceReportMapper;
import capstone.voicereport.service.VoiceReportService;
import jakarta.annotation.PreDestroy;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

final class MixedVoiceLoadHarness {

    private final long downstreamDelayMs;
    private final AtomicInteger ids = new AtomicInteger();
    private final ConcurrentMap<Integer, JobRecord> jobs = new ConcurrentHashMap<>();
    private final InMemoryMediaStorage storage = new InMemoryMediaStorage();
    private final ThreadPoolExecutor workers;
    private final AtomicInteger peakQueueDepth = new AtomicInteger();
    private final AtomicInteger completed = new AtomicInteger();
    private final AtomicInteger failed = new AtomicInteger();
    private final AtomicReference<CountDownLatch> completionLatch =
            new AtomicReference<>(new CountDownLatch(0));
    private final VoiceReportService service;

    MixedVoiceLoadHarness(int workerThreads, long downstreamDelayMs) {
        this.downstreamDelayMs = downstreamDelayMs;
        workers = new ThreadPoolExecutor(
                workerThreads,
                workerThreads,
                0,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>()
        );

        VoiceReportJobStateService state = stateService();
        VoiceReportJobProcessor processor = new VoiceReportJobProcessor(
                state, storage, this::analyze
        );
        VoiceReportJobPublisher publisher = message -> {
            CountDownLatch runLatch = completionLatch.get();
            workers.execute(() -> {
                try {
                    processor.process(message);
                } finally {
                    runLatch.countDown();
                }
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
        assertEquals(0, workers.getActiveCount());
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

    private VoiceReportJobStateService stateService() {
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
        }).when(state).attachMediaObjectKey(anyInt(), anyString());
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
            jobs.get((int) invocation.getArgument(0)).status = VoiceReportStatus.COMPLETED;
            completed.incrementAndGet();
            return null;
        }).when(state).complete(anyInt(), any(VoiceReportResponse.class));
        doAnswer(invocation -> {
            jobs.get((int) invocation.getArgument(0)).status = VoiceReportStatus.FAILED;
            failed.incrementAndGet();
            return null;
        }).when(state).fail(anyInt(), anyString());
        doAnswer(invocation -> {
            jobs.get((int) invocation.getArgument(0)).status = VoiceReportStatus.FAILED;
            failed.incrementAndGet();
            return null;
        }).when(state).failPendingSubmission(anyInt(), anyString());
        return state;
    }

    private VoiceReportResponse analyze(
            byte[] sourceMedia,
            String sourceName,
            int userId,
            int reportId
    ) {
        try {
            Thread.sleep(downstreamDelayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted fake processing", e);
        }
        VoiceReportResponse response = new VoiceReportResponse();
        response.setReportId(reportId);
        return response;
    }

    @PreDestroy
    void shutdown() {
        workers.shutdownNow();
    }

    @RestController
    static final class SynchronousController {
        private final long downstreamDelayMs;
        private final AtomicInteger active = new AtomicInteger();
        private final AtomicInteger peakActive = new AtomicInteger();

        SynchronousController(long downstreamDelayMs) {
            this.downstreamDelayMs = downstreamDelayMs;
        }

        @PostMapping(
                value = "/test/mixed/synchronous-voice",
                consumes = MediaType.MULTIPART_FORM_DATA_VALUE
        )
        ResponseEntity<Void> create(@RequestPart("video") MultipartFile media)
                throws InterruptedException {
            if (media.isEmpty()) return ResponseEntity.badRequest().build();
            int nowActive = active.incrementAndGet();
            peakActive.accumulateAndGet(nowActive, Math::max);
            try {
                Thread.sleep(downstreamDelayMs);
                return ResponseEntity.ok().build();
            } finally {
                active.decrementAndGet();
            }
        }

        void beginRun() {
            assertEquals(0, active.get());
            peakActive.set(0);
        }

        int peakActive() {
            return peakActive.get();
        }
    }

    private static final class InMemoryMediaStorage implements VoiceReportMediaStorage {
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

    private static final class JobRecord {
        private final int userId;
        private volatile String mediaObjectKey;
        private volatile VoiceReportStatus status = VoiceReportStatus.PENDING;

        private JobRecord(int userId) {
            this.userId = userId;
        }
    }
}
