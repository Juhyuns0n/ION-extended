package capstone.voicereport.async;

import capstone.voicereport.dto.VoiceReportResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VoiceReportJobProcessor {

    private final VoiceReportJobStateService stateService;
    private final VoiceReportMediaStorage mediaStorage;
    private final VoiceReportAnalysisProcessor analysisProcessor;

    public boolean process(VoiceReportJobMessage message) {
        VoiceReportClaim claim = stateService.claim(message);
        if (claim == VoiceReportClaim.ACTIVE) {
            log.info("Leaving active duplicate voice-report job {} unacknowledged", message.reportId());
            return false;
        }
        if (claim == VoiceReportClaim.TERMINAL) {
            log.info("Acknowledging terminal duplicate voice-report job {}", message.reportId());
            return true;
        }

        try {
            VoiceReportJobContext context = stateService.getContext(message.reportId());
            byte[] sourceMedia = mediaStorage.load(message.mediaObjectKey());
            VoiceReportResponse result = analysisProcessor.analyze(
                    sourceMedia, message.mediaObjectKey(), context.userId(), message.reportId()
            );
            stateService.complete(message.reportId(), result);
        } catch (Exception e) {
            log.error("Voice-report job {} failed", message.reportId(), e);
            stateService.fail(message.reportId(), "Processing failed: " + rootType(e));
        } finally {
            try {
                mediaStorage.delete(message.mediaObjectKey());
            } catch (Exception e) {
                log.warn("Unable to delete source media for voice-report job {}", message.reportId(), e);
            }
        }
        return true;
    }

    private String rootType(Exception exception) {
        Throwable current = exception;
        while (current.getCause() != null) current = current.getCause();
        return current.getClass().getSimpleName();
    }
}
