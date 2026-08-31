package capstone.voicereport;

import capstone.voicereport.service.ExistingVoiceReportAnalysisProcessor;
import capstone.voicereport.service.PythonAnalysisClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class ExistingVoiceReportAnalysisProcessorTest {

    @TempDir
    Path tempDir;

    @Test
    void ffmpegTimeoutDoesNotWaitForProcessOutputToClose() throws Exception {
        Path fakeFfmpeg = tempDir.resolve("fake-ffmpeg");
        Files.writeString(fakeFfmpeg, "#!/bin/sh\nexec sleep 5\n");
        assertTrue(fakeFfmpeg.toFile().setExecutable(true));

        PythonAnalysisClient pythonClient = mock(PythonAnalysisClient.class);
        ExistingVoiceReportAnalysisProcessor processor =
                new ExistingVoiceReportAnalysisProcessor(pythonClient);
        ReflectionTestUtils.setField(processor, "ffmpegPath", fakeFfmpeg.toString());
        ReflectionTestUtils.setField(processor, "convertTimeoutSec", 1L);

        IllegalStateException error = assertTimeout(Duration.ofSeconds(3), () ->
                assertThrows(IllegalStateException.class, () ->
                        processor.analyze(new byte[]{1}, "input.mp4", 7, 42)
                )
        );

        assertEquals("ffmpeg timed out after 1 seconds", error.getCause().getMessage());
        verifyNoInteractions(pythonClient);
    }
}
