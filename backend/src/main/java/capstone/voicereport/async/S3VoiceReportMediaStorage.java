package capstone.voicereport.async;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class S3VoiceReportMediaStorage implements VoiceReportMediaStorage {

    private final S3Client s3Client;
    private final VoiceReportAsyncProperties properties;

    @Override
    public String store(int reportId, String originalFilename, InputStream data, long contentLength, String contentType) {
        requireBucket();
        String mediaObjectKey = "voice-reports/%d/input%s".formatted(reportId, extensionOf(originalFilename));
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(properties.bucket())
                        .key(mediaObjectKey)
                        .contentType(StringUtils.hasText(contentType) ? contentType : "application/octet-stream")
                        .build(),
                RequestBody.fromInputStream(data, contentLength)
        );
        return mediaObjectKey;
    }

    @Override
    public byte[] load(String mediaObjectKey) {
        requireBucket();
        try {
            return s3Client.getObject(GetObjectRequest.builder()
                            .bucket(properties.bucket())
                            .key(mediaObjectKey)
                            .build())
                    .readAllBytes();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read voice-report media from object storage", e);
        }
    }

    @Override
    public void delete(String mediaObjectKey) {
        if (!StringUtils.hasText(mediaObjectKey)) return;
        requireBucket();
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(properties.bucket())
                .key(mediaObjectKey)
                .build());
    }

    private void requireBucket() {
        if (!StringUtils.hasText(properties.bucket())) {
            throw new IllegalStateException("VOICE_REPORT_S3_BUCKET is not configured");
        }
    }

    private String extensionOf(String filename) {
        if (!StringUtils.hasText(filename)) return ".media";
        String cleaned = StringUtils.cleanPath(filename);
        int dot = cleaned.lastIndexOf('.');
        String extension = dot >= 0 ? cleaned.substring(dot).toLowerCase() : ".media";
        return extension.matches("\\.[a-z0-9]{1,10}") ? extension : ".media";
    }
}
