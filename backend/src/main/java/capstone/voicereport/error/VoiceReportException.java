package capstone.voicereport.error;

import capstone.common.error.AppException;
import capstone.common.error.ErrorCode;

public class VoiceReportException extends AppException {

    private VoiceReportException(ErrorCode code, String message, String details) {
        super(code, message, details);
    }

    public static VoiceReportException notFound() {
        return new VoiceReportException(ErrorCode.VR_NOT_FOUND, null, null);
    }

    public static VoiceReportException forbidden(String details) {
        return new VoiceReportException(ErrorCode.VR_FORBIDDEN, null, details);
    }

    public static VoiceReportException payloadEmpty() {
        return new VoiceReportException(ErrorCode.VR_PAYLOAD_EMPTY, null, null);
    }

    public static VoiceReportException uploadError(String details) {
        return new VoiceReportException(ErrorCode.VR_UPLOAD_ERROR, null, details);
    }

    public static VoiceReportException analysisError(String details) {
        return new VoiceReportException(ErrorCode.VR_ANALYSIS_ERROR, null, details);
    }

    public static VoiceReportException timeout(String details) {
        return new VoiceReportException(ErrorCode.VR_TIMEOUT, null, details);
    }

    public static VoiceReportException loginRequired() {
        return new VoiceReportException(ErrorCode.VR_LOGIN_REQUIRED, null, null);
    }
}
