package com.jobspring.job.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobspring.job.client.AuthClient;
import com.jobspring.job.dto.JobDTO;
import com.jobspring.job.dto.JobSummaryResponse;
import com.jobspring.job.dto.JobSummaryDTO;
import com.jobspring.job.entity.Job;
import com.jobspring.job.repository.JobRepository;
import com.jobspring.job.service.JobService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// 只测试 MVC 切片；关闭安全过滤器避免 @PreAuthorize 影响不相关用例
@WebMvcTest(controllers = JobController.class)
@AutoConfigureMockMvc(addFilters = false)
class JobControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper om;

    @MockBean JobService jobService;
    @MockBean AuthClient authClient;
    @MockBean JobRepository jobRepository;

    @Test
    void job_list_should_return_page_of_dto() throws Exception {
        JobDTO dto = new JobDTO();
        dto.setId(100L);
        dto.setTitle("Java Dev");
        dto.setCompanyId(7L);
        dto.setCompany("ACME");
        dto.setLocation("SG");
        dto.setEmploymentType("Full-time");
        dto.setSalaryMin(new BigDecimal("8000"));
        dto.setSalaryMax(new BigDecimal("12000"));
        dto.setPostedAt(LocalDateTime.now());
        dto.setDescription("desc");

        Pageable pageable = PageRequest.of(0, 10, Sort.by("postedAt").descending());
        Page<JobDTO> page = new PageImpl<>(java.util.List.of(dto), pageable, 1);

        when(jobService.getJobSeekerJobs(
                org.mockito.ArgumentMatchers.any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(get("/job_list")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "postedAt")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id", is(100)))
                .andExpect(jsonPath("$.content[0].title", is("Java Dev")))
                .andExpect(jsonPath("$.content[0].company", is("ACME")));
    }

    @Test
    void summary_should_delegate_to_service() throws Exception {
        JobSummaryResponse r = new JobSummaryResponse();
        r.setId(100L);
        r.setTitle("Java Dev");
        r.setCompanyName("ACME");
        r.setStatus(0);
        r.setEmploymentType(1);
        r.setPostedAt(LocalDateTime.now());

        when(jobService.getSummary(100L)).thenReturn(r);

        mockMvc.perform(get("/100/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(100)))
                .andExpect(jsonPath("$.companyName", is("ACME")))
                .andExpect(jsonPath("$.status", is(0)));
    }

    @Test
    void apply_summary_should_read_from_repository_directly() throws Exception {
        Job j = new Job();
        j.setId(100L); j.setStatus(0); j.setCompanyId(7L); j.setTitle("Java Dev");
        when(jobRepository.findById(100L)).thenReturn(Optional.of(j));

        mockMvc.perform(get("/100/apply/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(100)))
                .andExpect(jsonPath("$.status", is(0)))
                .andExpect(jsonPath("$.companyId", is(7)))
                .andExpect(jsonPath("$.title", is("Java Dev")));
    }
}
