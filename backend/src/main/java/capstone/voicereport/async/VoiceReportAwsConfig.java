package capstone.voicereport.async;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
@EnableScheduling
public class VoiceReportAwsConfig {

    @Bean
    S3Client voiceReportS3Client(VoiceReportAsyncProperties properties) {
        return S3Client.builder().region(Region.of(properties.region())).build();
    }

    @Bean
    SqsClient voiceReportSqsClient(VoiceReportAsyncProperties properties) {
        return SqsClient.builder().region(Region.of(properties.region())).build();
    }
}
