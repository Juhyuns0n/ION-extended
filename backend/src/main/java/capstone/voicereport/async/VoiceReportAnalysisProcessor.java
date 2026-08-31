package capstone.voicereport.async;

import capstone.voicereport.dto.VoiceReportResponse;

public interface VoiceReportAnalysisProcessor {
    VoiceReportResponse analyze(byte[] sourceMedia, String sourceName, int userId, int reportId);
}
