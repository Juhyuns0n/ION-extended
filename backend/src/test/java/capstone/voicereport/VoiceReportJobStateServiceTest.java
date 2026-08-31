package capstone.voicereport;

import capstone.home.entity.UserProfile;
import capstone.home.repository.UserProfileRepository;
import capstone.user.entity.User;
import capstone.user.repository.UserRepository;
import capstone.voicereport.async.*;
import capstone.voicereport.dto.VoiceReportResponse;
import capstone.voicereport.entity.VoiceReport;
import capstone.voicereport.repository.VoiceReportRepository;
import capstone.voicereport.service.VoiceReportMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VoiceReportJobStateServiceTest {

    private VoiceReportRepository reports;
    private UserRepository users;
    private UserProfileRepository profiles;
    private VoiceReportMapper mapper;
    private VoiceReportJobStateService state;

    @BeforeEach
    void setUp() {
        reports = mock(VoiceReportRepository.class);
        users = mock(UserRepository.class);
        profiles = mock(UserProfileRepository.class);
        mapper = mock(VoiceReportMapper.class);
        state = new VoiceReportJobStateService(
                reports, users, profiles, mapper,
                new VoiceReportAsyncProperties("ap-northeast-2", "bucket", "queue", false, 1000, 10, 900, 1)
        );
    }

    @Test
    void createPendingPersistsLifecycleState() {
        when(users.findByUserId(7)).thenReturn(Optional.of(new User()));
        when(reports.saveAndFlush(any())).thenAnswer(invocation -> {
            VoiceReport report = invocation.getArgument(0);
            report.setReportId(42);
            return report;
        });

        VoiceReport report = state.createPending(7);

        assertEquals(42, report.getReportId());
        assertEquals(VoiceReportStatus.PENDING, report.getProcessingStatus());
        verify(reports).saveAndFlush(report);
    }

    @Test
    void pendingTransitionsThroughProcessingToCompletedOnce() {
        VoiceReport report = pendingReport();
        when(reports.findByReportIdForUpdate(42)).thenReturn(Optional.of(report));
        UserProfile profile = UserProfile.builder().points(0).voicereportFrequency(0).build();
        when(profiles.findByUserId(7)).thenReturn(Optional.of(profile));

        assertEquals(VoiceReportClaim.CLAIMED, state.claim(message()));
        assertEquals(VoiceReportStatus.PROCESSING, report.getProcessingStatus());

        VoiceReportResponse result = new VoiceReportResponse();
        state.complete(42, result);

        assertEquals(VoiceReportStatus.COMPLETED, report.getProcessingStatus());
        assertNotNull(report.getCompletedAt());
        assertEquals(4, profile.getPoints());
        assertEquals(1, profile.getVoicereportFrequency());
        verify(mapper).applyAnalysis(report, result);
    }

    @Test
    void duplicateDeliveryDoesNotReclaimActiveProcessingJob() {
        VoiceReport report = pendingReport();
        when(reports.findByReportIdForUpdate(42)).thenReturn(Optional.of(report));

        assertEquals(VoiceReportClaim.CLAIMED, state.claim(message()));
        assertEquals(VoiceReportClaim.ACTIVE, state.claim(message()));
    }

    @Test
    void staleProcessingJobCanBeReclaimed() {
        VoiceReport report = pendingReport();
        report.setProcessingStatus(VoiceReportStatus.PROCESSING);
        report.setProcessingStartedAt(LocalDateTime.now().minusSeconds(5));
        when(reports.findByReportIdForUpdate(42)).thenReturn(Optional.of(report));

        assertEquals(VoiceReportClaim.CLAIMED, state.claim(message()));
        assertTrue(report.getProcessingStartedAt().isAfter(LocalDateTime.now().minusSeconds(1)));
    }

    @Test
    void processingFailureBecomesTerminalFailedState() {
        VoiceReport report = pendingReport();
        report.setProcessingStatus(VoiceReportStatus.PROCESSING);
        when(reports.findByReportIdForUpdate(42)).thenReturn(Optional.of(report));

        state.fail(42, "deterministic test failure");

        assertEquals(VoiceReportStatus.FAILED, report.getProcessingStatus());
        assertNotNull(report.getCompletedAt());
        assertEquals("deterministic test failure", report.getFailureReason());
    }

    private VoiceReport pendingReport() {
        VoiceReport report = new VoiceReport();
        report.setReportId(42);
        report.setUserId(7);
        report.setMediaObjectKey("voice-reports/42/input.mp4");
        report.setProcessingStatus(VoiceReportStatus.PENDING);
        return report;
    }

    private VoiceReportJobMessage message() {
        return new VoiceReportJobMessage(42, "voice-reports/42/input.mp4");
    }
}
