package capstone.common.error;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @Data
    @AllArgsConstructor
    public static class ErrorBody {
        private String code;
        private String message;
        private String path;
    }

    // 413: 진짜 '바이트 용량 초과'만 여기로
    @ExceptionHandler({ MaxUploadSizeExceededException.class, DataBufferLimitException.class })
    public ResponseEntity<ErrorBody> handlePayloadTooLarge(Exception e, HttpServletRequest req) {
        ErrorCode code = ErrorCode.PAYLOAD_TOO_LARGE;
        log.warn("[413] payload too large: {} path={}", e.getMessage(), req.getRequestURI());
        return ResponseEntity.status(code.getStatus())
                .body(new ErrorBody(code.name(), code.getDefaultMessage(), req.getRequestURI()));
    }

    // AppException 공통 처리 (VoiceReportException 포함)
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorBody> handleApp(AppException e, HttpServletRequest req) {
        ErrorCode code = e.getCode();
        String message = (e.getMessage() != null && !e.getMessage().isBlank())
                ? e.getMessage()
                : code.getDefaultMessage();

        log.warn("[APP-EX] code={} status={} msg={} path={}",
                code.name(), code.getStatus().value(), message, req.getRequestURI());

        return ResponseEntity.status(code.getStatus())
                .body(new ErrorBody(code.name(), message, req.getRequestURI()));
    }

    // 그 외 진짜 예측 못 한 에러
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorBody> handleAny(Exception e, HttpServletRequest req) {
        log.error("[500] ex={} msg={} path={}", e.getClass().getName(), e.getMessage(), req.getRequestURI(), e);
        ErrorCode code = ErrorCode.INTERNAL_ERROR;
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorBody(code.name(), code.getDefaultMessage(), req.getRequestURI()));
    }
}

