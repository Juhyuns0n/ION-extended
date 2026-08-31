package capstone.voicereport;

import capstone.voicereport.async.VoiceReportStatus;
import capstone.voicereport.controller.VoiceReportController;
import capstone.voicereport.dto.VoiceReportJobResponse;
import capstone.voicereport.dto.VoiceReportSubmissionResponse;
import capstone.voicereport.service.VoiceReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class VoiceReportControllerAsyncTest {

    private VoiceReportService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(VoiceReportService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new VoiceReportController(service)).build();
    }

    @Test
    void submissionReturns202AndStatusLocation() throws Exception {
        when(service.submit(org.mockito.ArgumentMatchers.eq(7), any()))
                .thenReturn(new VoiceReportSubmissionResponse(42, VoiceReportStatus.PENDING));

        mockMvc.perform(multipart("/api/voice-reports")
                        .file(new MockMultipartFile("video", "input.mp4", "video/mp4", new byte[]{1}))
                        .sessionAttr("userId", 7))
                .andExpect(status().isAccepted())
                .andExpect(header().string("Location", "/api/voice-reports/42/status"))
                .andExpect(jsonPath("$.reportId").value(42))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void statusEndpointReturnsLifecycleState() throws Exception {
        when(service.getStatus(7, 42))
                .thenReturn(new VoiceReportJobResponse(42, VoiceReportStatus.PROCESSING, null, null));

        mockMvc.perform(get("/api/voice-reports/42/status").sessionAttr("userId", 7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportId").value(42))
                .andExpect(jsonPath("$.status").value("PROCESSING"))
                .andExpect(jsonPath("$.report").doesNotExist());
    }
}
