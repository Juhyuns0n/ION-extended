package capstone.voicereport.async;

import capstone.home.entity.UserProfile;
import capstone.home.repository.UserProfileRepository;
import capstone.user.repository.UserRepository;
import capstone.voicereport.dto.VoiceReportJobResponse;
import capstone.voicereport.dto.VoiceReportResponse;
import capstone.voicereport.entity.VoiceReport;
import capstone.voicereport.error.VoiceReportException;
import capstone.voicereport.repository.VoiceReportRepository;
import capstone.voicereport.service.VoiceReportMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class VoiceReportJobStateService {

    private final VoiceReportRepository reportRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final VoiceReportMapper mapper;
    private final VoiceReportAsyncProperties properties;

    @Transactional
    public VoiceReport createPending(int userId) {
        userRepository.findByUserId(userId)
                .orElseThrow(() -> VoiceReportException.uploadError("user not found: " + userId));
        VoiceReport report = new VoiceReport();
        report.setUserId(userId);
        report.setProcessingStatus(VoiceReportStatus.PENDING);
        return reportRepository.saveAndFlush(report);
    }

    @Transactional
    public void attachMediaObjectKey(int reportId, String mediaObjectKey) {
        VoiceReport report = locked(reportId);
        if (report.getProcessingStatus() != VoiceReportStatus.PENDING) {
            throw new IllegalStateException("Media can only be attached to a pending job");
        }
        report.setMediaObjectKey(mediaObjectKey);
    }

    @Transactional
    public VoiceReportClaim claim(VoiceReportJobMessage message) {
        VoiceReport report = locked(message.reportId());
        if (!Objects.equals(report.getMediaObjectKey(), message.mediaObjectKey())) {
            throw new IllegalArgumentException("Queue message does not match the persisted job");
        }

        if (report.getProcessingStatus() == VoiceReportStatus.COMPLETED
                || report.getProcessingStatus() == VoiceReportStatus.FAILED) {
            return VoiceReportClaim.TERMINAL;
        }

        boolean pending = report.getProcessingStatus() == VoiceReportStatus.PENDING;
        boolean stale = report.getProcessingStatus() == VoiceReportStatus.PROCESSING
                && (report.getProcessingStartedAt() == null
                || report.getProcessingStartedAt().isBefore(
                        LocalDateTime.now().minusSeconds(Math.max(1, properties.processingLeaseSeconds()))
                ));
        if (!pending && !stale) return VoiceReportClaim.ACTIVE;

        report.setProcessingStatus(VoiceReportStatus.PROCESSING);
        report.setProcessingStartedAt(LocalDateTime.now());
        report.setFailureReason(null);
        return VoiceReportClaim.CLAIMED;
    }

    @Transactional
    public void complete(int reportId, VoiceReportResponse analysis) {
        VoiceReport report = locked(reportId);
        if (report.getProcessingStatus() == VoiceReportStatus.COMPLETED) return;
        if (report.getProcessingStatus() != VoiceReportStatus.PROCESSING) {
            throw new IllegalStateException("Only a processing job can be completed");
        }
        mapper.applyAnalysis(report, analysis);
        report.setProcessingStatus(VoiceReportStatus.COMPLETED);
        report.setCompletedAt(LocalDateTime.now());
        report.setFailureReason(null);

        UserProfile profile = userProfileRepository.findByUserId(report.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User profile not found"));
        profile.setUsedVoiceReportOnce(1);
        profile.setVoicereportFrequency(valueOrZero(profile.getVoicereportFrequency()) + 1);
        profile.setPoints(valueOrZero(profile.getPoints()) + 4);
    }

    @Transactional
    public void fail(int reportId, String reason) {
        VoiceReport report = locked(reportId);
        if (report.getProcessingStatus() == VoiceReportStatus.COMPLETED
                || report.getProcessingStatus() == VoiceReportStatus.FAILED) return;
        report.setProcessingStatus(VoiceReportStatus.FAILED);
        report.setFailureReason(safeReason(reason));
        report.setCompletedAt(LocalDateTime.now());
    }

    @Transactional
    public void failPendingSubmission(int reportId, String reason) {
        VoiceReport report = locked(reportId);
        if (report.getProcessingStatus() != VoiceReportStatus.PENDING) return;
        report.setProcessingStatus(VoiceReportStatus.FAILED);
        report.setFailureReason(safeReason(reason));
        report.setCompletedAt(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public VoiceReportJobContext getContext(int reportId) {
        VoiceReport report = reportRepository.findById(reportId)
                .orElseThrow(VoiceReportException::notFound);
        return new VoiceReportJobContext(report.getUserId(), report.getMediaObjectKey());
    }

    @Transactional(readOnly = true)
    public VoiceReportJobResponse getStatus(int userId, int reportId) {
        VoiceReport report = reportRepository.findByReportIdAndUserId(reportId, userId)
                .orElseThrow(VoiceReportException::notFound);
        VoiceReportResponse result = report.getProcessingStatus() == VoiceReportStatus.COMPLETED
                ? mapper.toDto(report)
                : null;
        String error = report.getProcessingStatus() == VoiceReportStatus.FAILED
                ? "Voice report processing failed"
                : null;
        return new VoiceReportJobResponse(reportId, report.getProcessingStatus(), result, error);
    }

    private VoiceReport locked(int reportId) {
        return reportRepository.findByReportIdForUpdate(reportId)
                .orElseThrow(VoiceReportException::notFound);
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private String safeReason(String reason) {
        if (reason == null || reason.isBlank()) return "Unspecified processing failure";
        return reason.length() <= 1000 ? reason : reason.substring(0, 1000);
    }
}
