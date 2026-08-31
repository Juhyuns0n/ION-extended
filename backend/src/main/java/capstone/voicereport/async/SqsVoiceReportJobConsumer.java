package capstone.voicereport.async;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "voice-report.async", name = "consumer-enabled", havingValue = "true")
public class SqsVoiceReportJobConsumer {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final VoiceReportAsyncProperties properties;
    private final VoiceReportJobProcessor processor;

    @Scheduled(fixedDelayString = "${voice-report.async.poll-interval-ms:1000}")
    public void poll() {
        if (!StringUtils.hasText(properties.queueUrl())) {
            log.error("Voice-report consumer is enabled but VOICE_REPORT_SQS_QUEUE_URL is missing");
            return;
        }

        for (Message message : sqsClient.receiveMessage(ReceiveMessageRequest.builder()
                .queueUrl(properties.queueUrl())
                .maxNumberOfMessages(5)
                .waitTimeSeconds(properties.waitTimeSeconds())
                .visibilityTimeout(properties.visibilityTimeoutSeconds())
                .build()).messages()) {
            try {
                VoiceReportJobMessage job = objectMapper.readValue(message.body(), VoiceReportJobMessage.class);
                if (processor.process(job)) {
                    sqsClient.deleteMessage(DeleteMessageRequest.builder()
                            .queueUrl(properties.queueUrl())
                            .receiptHandle(message.receiptHandle())
                            .build());
                }
            } catch (Exception e) {
                log.error("Voice-report queue message was not acknowledged; SQS may redeliver it", e);
            }
        }
    }
}
