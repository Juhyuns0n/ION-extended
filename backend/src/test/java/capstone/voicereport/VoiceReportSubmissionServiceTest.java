package capstone.voicereport;

import capstone.voicereport.async.*;
import capstone.voicereport.dto.VoiceReportSubmissionResponse;
import capstone.voicereport.entity.VoiceReport;
import capstone.voicereport.repository.VoiceReportRepository;
import capstone.voicereport.service.VoiceReportMapper;
import capstone.voicereport.service.VoiceReportService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class VoiceReportSubmissionServiceTest {

    @Test
    void persistsPendingStoresMediaAndPublishesSmallMessage() throws Exception {
        VoiceReportRepository repository = mock(VoiceReportRepository.class);
        VoiceReportJobStateService state = mock(VoiceReportJobStateService.class);
        VoiceReportMediaStorage storage = mock(VoiceReportMediaStorage.class);
        VoiceReportJobPublisher publisher = mock(VoiceReportJobPublisher.class);
        VoiceReportMapper mapper = mock(VoiceReportMapper.class);
        VoiceReportService service = new VoiceReportService(repository, state, storage, publisher, mapper);

        VoiceReport pending = new VoiceReport();
        pending.setReportId(42);
        pending.setUserId(7);
        pending.setProcessingStatus(VoiceReportStatus.PENDING);
        when(state.createPending(7)).thenReturn(pending);
        when(storage.store(eq(42), eq("input.mp4"), any(), eq(3L), eq("video/mp4")))
                .thenReturn("voice-reports/42/input.mp4");

        VoiceReportSubmissionResponse response = service.submit(
                7,
                new MockMultipartFile("video", "input.mp4", "video/mp4", new byte[]{1, 2, 3})
        );

        assertEquals(42, response.reportId());
        assertEquals(VoiceReportStatus.PENDING, response.status());
        verify(state).createPending(7);
        verify(state).attachMediaObjectKey(42, "voice-reports/42/input.mp4");
        verify(publisher).publish(new VoiceReportJobMessage(42, "voice-reports/42/input.mp4"));
    }
}
