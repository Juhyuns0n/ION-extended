package capstone.voicereport.service;

import capstone.voicereport.async.VoiceReportAnalysisProcessor;
import capstone.voicereport.dto.VoiceReportResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExistingVoiceReportAnalysisProcessor implements VoiceReportAnalysisProcessor {

    private static final int MAX_DIAGNOSTIC_BYTES = 8 * 1024;

    private final PythonAnalysisClient pythonAnalysisClient;

    @Value("${media.ffmpeg.path:ffmpeg}")
    private String ffmpegPath;

    @Value("${media.convert.timeoutSec:120}")
    private long convertTimeoutSec;

    @Override
    public VoiceReportResponse analyze(byte[] sourceMedia, String sourceName, int userId, int reportId) {
        Path input = null;
        Path wav = null;
        try {
            input = Files.createTempFile("voice-report-", extensionOf(sourceName));
            wav = Files.createTempFile("voice-report-", ".wav");
            Files.write(input, sourceMedia);
            convertToWav16kMono(input, wav);
            return pythonAnalysisClient.analyze(Files.readAllBytes(wav), userId, reportId);
        } catch (Exception e) {
            throw new IllegalStateException("Voice-report analysis failed", e);
        } finally {
            deleteQuietly(input);
            deleteQuietly(wav);
        }
    }

    private void convertToWav16kMono(Path input, Path output) throws Exception {
        List<String> command = List.of(
                ffmpegPath, "-y", "-i", input.toAbsolutePath().toString(),
                "-vn", "-ac", "1", "-ar", "16000", "-acodec", "pcm_s16le",
                "-f", "wav", output.toAbsolutePath().toString()
        );
        Process process = new ProcessBuilder(command).start();
        FutureTask<String> stderrTask = new FutureTask<>(
                () -> readBoundedDiagnostics(process.getErrorStream())
        );
        Thread stderrReader = new Thread(stderrTask, "voice-report-ffmpeg-stderr");
        stderrReader.setDaemon(true);
        stderrReader.start();

        if (!process.waitFor(convertTimeoutSec, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            process.waitFor(5, TimeUnit.SECONDS);
            throw new IllegalStateException("ffmpeg timed out after " + convertTimeoutSec + " seconds");
        }
        String stderr = awaitDiagnostics(stderrTask);
        if (process.exitValue() != 0 || !Files.exists(output) || Files.size(output) == 0) {
            throw new IllegalStateException("ffmpeg failed with exit " + process.exitValue() + ": " + stderr);
        }
    }

    private String readBoundedDiagnostics(InputStream input) throws Exception {
        try (input; ByteArrayOutputStream captured = new ByteArrayOutputStream(MAX_DIAGNOSTIC_BYTES)) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = input.read(buffer)) != -1) {
                int remaining = MAX_DIAGNOSTIC_BYTES - captured.size();
                if (remaining > 0) captured.write(buffer, 0, Math.min(read, remaining));
            }
            return captured.toString(StandardCharsets.UTF_8);
        }
    }

    private String awaitDiagnostics(FutureTask<String> stderrTask) {
        try {
            return stderrTask.get(1, TimeUnit.SECONDS);
        } catch (Exception ignored) {
            return "diagnostics unavailable";
        }
    }

    private String extensionOf(String sourceName) {
        if (!StringUtils.hasText(sourceName)) return ".media";
        int dot = sourceName.lastIndexOf('.');
        String extension = dot >= 0 ? sourceName.substring(dot) : ".media";
        return extension.matches("\\.[A-Za-z0-9]{1,10}") ? extension : ".media";
    }

    private void deleteQuietly(Path path) {
        try {
            if (path != null) Files.deleteIfExists(path);
        } catch (Exception e) {
            log.warn("Unable to delete temporary voice-report file {}", path);
        }
    }
}
