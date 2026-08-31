package capstone.voicereport.async;

import java.io.InputStream;

public interface VoiceReportMediaStorage {
    String store(int reportId, String originalFilename, InputStream data, long contentLength, String contentType);

    byte[] load(String mediaObjectKey);

    void delete(String mediaObjectKey);
}
