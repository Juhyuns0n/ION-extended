package capstone.voicereport.dto;

import capstone.voicereport.async.VoiceReportStatus;

public record VoiceReportJobResponse(
        int reportId,
        VoiceReportStatus status,
        VoiceReportResponse report,
        String errorMessage
) {
}
