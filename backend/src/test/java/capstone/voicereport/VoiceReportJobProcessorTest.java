package capstone.voicereport;

import capstone.voicereport.async.*;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class VoiceReportJobProcessorTest {

    @Test
    void duplicateDeliveryDoesNotRunAnalysisAgain() {
        VoiceReportJobStateService state = mock(VoiceReportJobStateService.class);
        VoiceReportMediaStorage storage = mock(VoiceReportMediaStorage.class);
        VoiceReportAnalysisProcessor analysis = mock(VoiceReportAnalysisProcessor.class);
        VoiceReportJobProcessor processor = new VoiceReportJobProcessor(state, storage, analysis);
        VoiceReportJobMessage message = new VoiceReportJobMessage(42, "voice-reports/42/input.mp4");
        when(state.claim(message)).thenReturn(VoiceReportClaim.ACTIVE);

        org.junit.jupiter.api.Assertions.assertFalse(processor.process(message));

        verifyNoInteractions(storage, analysis);
    }

    @Test
    void terminalAnalysisFailureIsPersistedAndMediaIsDeleted() {
        VoiceReportJobStateService state = mock(VoiceReportJobStateService.class);
        VoiceReportMediaStorage storage = mock(VoiceReportMediaStorage.class);
        VoiceReportAnalysisProcessor analysis = mock(VoiceReportAnalysisProcessor.class);
        VoiceReportJobProcessor processor = new VoiceReportJobProcessor(state, storage, analysis);
        VoiceReportJobMessage message = new VoiceReportJobMessage(42, "voice-reports/42/input.mp4");
        when(state.claim(message)).thenReturn(VoiceReportClaim.CLAIMED);
        when(state.getContext(42)).thenReturn(new VoiceReportJobContext(7, message.mediaObjectKey()));
        when(storage.load(message.mediaObjectKey())).thenReturn(new byte[]{1});
        when(analysis.analyze(any(), anyString(), eq(7), eq(42)))
                .thenThrow(new IllegalStateException("deterministic test failure"));

        processor.process(message);

        verify(state).fail(42, "Processing failed: IllegalStateException");
        verify(storage).delete(message.mediaObjectKey());
    }
}
