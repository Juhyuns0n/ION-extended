package capstone.voicereport.service;

import capstone.voicereport.async.*;
import capstone.voicereport.dto.*;
import capstone.voicereport.entity.EmotionPoint;
import capstone.voicereport.entity.VoiceReport;
import capstone.voicereport.error.VoiceReportException;
import capstone.voicereport.repository.VoiceReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class VoiceReportService {

    private final VoiceReportRepository reportRepository;
    private final VoiceReportJobStateService stateService;
    private final VoiceReportMediaStorage mediaStorage;
    private final VoiceReportJobPublisher jobPublisher;
    private final VoiceReportMapper mapper;

    public VoiceReportSubmissionResponse submit(int userId, MultipartFile media) {
        if (media == null || media.isEmpty()) throw VoiceReportException.payloadEmpty();

        VoiceReport pending = stateService.createPending(userId);
        String mediaObjectKey = null;
        try {
            mediaObjectKey = mediaStorage.store(
                    pending.getReportId(),
                    media.getOriginalFilename(),
                    media.getInputStream(),
                    media.getSize(),
                    media.getContentType()
            );
            stateService.attachMediaObjectKey(pending.getReportId(), mediaObjectKey);
            jobPublisher.publish(new VoiceReportJobMessage(pending.getReportId(), mediaObjectKey));
            return new VoiceReportSubmissionResponse(pending.getReportId(), VoiceReportStatus.PENDING);
        } catch (Exception e) {
            stateService.failPendingSubmission(pending.getReportId(), "Submission failed: " + e.getClass().getSimpleName());
            deleteUploadedMediaQuietly(mediaObjectKey);
            throw VoiceReportException.uploadError("Unable to submit voice report for asynchronous processing");
        }
    }

    @Transactional(readOnly = true)
    public VoiceReportResponse getOneVoicereport(int userId, int reportId) {
        VoiceReport report = reportRepository.findByReportIdAndUserId(reportId, userId)
                .filter(item -> item.getProcessingStatus() == VoiceReportStatus.COMPLETED)
                .orElseThrow(VoiceReportException::notFound);
        return mapper.toDto(report);
    }

    public VoiceReportJobResponse getStatus(int userId, int reportId) {
        return stateService.getStatus(userId, reportId);
    }

    @Transactional(readOnly = true)
    public Page<VoiceReportListResponse> list(int userId, Pageable pageable) {
        return reportRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Transactional(readOnly = true)
    public VoiceReportSummaryDto getSummary(int userId) {
        VoiceReport report = reportRepository
                .findTop1ByUserIdAndProcessingStatusOrderByReportIdDesc(userId, VoiceReportStatus.COMPLETED)
                .orElseThrow(VoiceReportException::notFound);

        VoiceReportSummaryDto dto = new VoiceReportSummaryDto();
        List<EmotionPoint> timeline = Optional.ofNullable(report.getEmotionTimeline()).orElseGet(List::of);
        dto.setMomentEmotion(timeline.isEmpty()
                ? null
                : timeline.get(ThreadLocalRandom.current().nextInt(timeline.size())).getMomentEmotion());
        if (report.getFrequency() != null) {
            dto.setParentFrequency(report.getFrequency().getParentFrequency());
            dto.setKidFrequency(report.getFrequency().getKidFrequency());
        }
        dto.setOverallFeedback(report.getOverallFeedback() == null ? null : report.getConversationSummary());
        return dto;
    }

    private void deleteUploadedMediaQuietly(String mediaObjectKey) {
        try {
            mediaStorage.delete(mediaObjectKey);
        } catch (Exception ignored) {
            // The failed submission remains persisted as FAILED for operational cleanup.
        }
    }
}
