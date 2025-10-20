package com.jobspring.user.controller;

import com.jobspring.user.client.CompanyClient;
import com.jobspring.user.client.JobClient;
import com.jobspring.user.controller.HrController;
import com.jobspring.user.dto.JobResponse;
import com.jobspring.user.service.HrCompanyService;
import org.springframework.security.test.context.support.WithMockUser;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = HrController.class)
@AutoConfigureMockMvc
@Import(HrControllerTest.MethodSecurityTestConfig.class)
class HrControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    HrCompanyService hrCompanyService;
    @MockBean JobClient jobClient;
    @MockBean CompanyClient companyClient;


    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityTestConfig {
    }

    // ---------- /hr/company-name ----------

    @Test
    @WithMockUser(roles = "HR")
    void getMyCompanyName_ok_whenRoleHR_andHeaderPresent() throws Exception {
        Long uid = 42L;
        when(hrCompanyService.getMyCompanyName(uid)).thenReturn("ACME");

        mvc.perform(get("/hr/company-name")
                        .header("X-User-Id", uid))
                .andExpect(status().isOk())
                .andExpect(content().string("ACME"));

        verify(hrCompanyService).getMyCompanyName(uid);
        verifyNoInteractions(jobClient, companyClient);
    }

    @Test
    @WithMockUser(roles = "USER") // 非 HR
    void getMyCompanyName_forbidden_whenNotHR() throws Exception {
        mvc.perform(get("/hr/company-name").header("X-User-Id", 1L))
                .andExpect(status().isForbidden());
        verifyNoInteractions(hrCompanyService, jobClient, companyClient);
    }

    // ---------- /hr/jobs-detail/{jobId} ----------

    @Test
    @WithMockUser(username = "123", roles = "HR") // Authentication.getName() == "123"
    void getJobDetailForEdit_ok_andCallsDownstreamWithCompanyId() throws Exception {
        long authUserId = 123L;
        long companyId  = 10L;
        long jobId      = 555L;

        var job = JobResponse.builder()
                .id(jobId)
                .title("Senior Backend Engineer")
                .companyId(companyId)
                .build();

        when(companyClient.findCompanyIdByHr(authUserId)).thenReturn(companyId);
        when(jobClient.getJobForEdit(companyId, jobId)).thenReturn(job);

        mvc.perform(get("/hr/jobs-detail/{jobId}", jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value((int) jobId))
                .andExpect(jsonPath("$.title").value("Senior Backend Engineer"))
                .andExpect(jsonPath("$.companyId").value((int) companyId));

        verify(companyClient).findCompanyIdByHr(authUserId);
        verify(jobClient).getJobForEdit(companyId, jobId);
        verifyNoInteractions(hrCompanyService);
    }

    @Test
    void getJobDetailForEdit_unauthenticated_isUnauthorized() throws Exception {
        mvc.perform(get("/hr/jobs-detail/{jobId}", 1L))
                .andExpect(status().isUnauthorized()); // 未登录
        verifyNoInteractions(companyClient, jobClient, hrCompanyService);
    }
}
