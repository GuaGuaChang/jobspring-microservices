package com.jobspring.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobspring.application.api.ApplicationController;
import com.jobspring.application.dto.ApplicationBriefResponse;
import com.jobspring.application.service.ApplicationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApplicationController.class)
public class ApplicationControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private ApplicationService applicationService;
    @Autowired private ObjectMapper mapper;

    @Test
    @WithMockUser(username = "2", roles = {"CANDIDATE"})
    void testListMine_success() throws Exception {
        ApplicationBriefResponse mockResp = new ApplicationBriefResponse();
        mockResp.setId(1L);
        mockResp.setJobTitle("Java Developer");
        mockResp.setAppliedAt(LocalDateTime.now());

        Page<ApplicationBriefResponse> mockPage = new PageImpl<>(List.of(mockResp));
        Mockito.when(applicationService.listMine(eq(2L), any(), any())).thenReturn(mockPage);

        mockMvc.perform(get("/getApplications")
                        .param("status", "0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].jobTitle").value("Java Developer"));
    }

    @Test
    @WithMockUser(username = "hrUser", roles = {"HR"})
    void testUpdateStatus_success() throws Exception {
        ApplicationBriefResponse mockRes = new ApplicationBriefResponse();
        mockRes.setId(1L);
        mockRes.setStatus(1);
        Mockito.when(applicationService.updateStatus(anyLong(), anyLong(), anyInt())).thenReturn(mockRes);

        mockMvc.perform(post("/applications/1/status")
                        .with(csrf())
                        .header("X-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1));
    }
}
