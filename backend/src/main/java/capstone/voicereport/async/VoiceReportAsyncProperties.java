package capstone.voicereport.async;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "voice-report.async")
public record VoiceReportAsyncProperties(
        String region,
        String bucket,
        String queueUrl,
        boolean consumerEnabled,
        long pollIntervalMs,
        int waitTimeSeconds,
        int visibilityTimeoutSeconds,
        int processingLeaseSeconds
) {
}
