package capstone.voicereport.async;

public interface VoiceReportJobPublisher {
    void publish(VoiceReportJobMessage message);
}
