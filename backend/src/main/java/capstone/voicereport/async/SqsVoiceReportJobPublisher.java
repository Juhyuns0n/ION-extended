package capstone.voicereport.async;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Component
@RequiredArgsConstructor
public class SqsVoiceReportJobPublisher implements VoiceReportJobPublisher {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final VoiceReportAsyncProperties properties;

    @Override
    public void publish(VoiceReportJobMessage message) {
        if (!StringUtils.hasText(properties.queueUrl())) {
            throw new IllegalStateException("VOICE_REPORT_SQS_QUEUE_URL is not configured");
        }
        try {
            sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(properties.queueUrl())
                    .messageBody(objectMapper.writeValueAsString(message))
                    .build());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize voice-report job", e);
        }
    }
}
