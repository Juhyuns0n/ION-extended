package capstone.voicereport.dto;

import capstone.voicereport.async.VoiceReportStatus;

public record VoiceReportSubmissionResponse(int reportId, VoiceReportStatus status) {
}
