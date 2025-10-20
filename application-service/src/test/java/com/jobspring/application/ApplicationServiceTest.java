package com.jobspring.application;

import com.jobspring.application.client.CompanyClient;
import com.jobspring.application.client.JobClient;
import com.jobspring.application.client.UserClient;
import com.jobspring.application.dto.*;
import com.jobspring.application.entity.Application;
import com.jobspring.application.repository.ApplicationRepository;
import com.jobspring.application.repository.PdfRepository;
import com.jobspring.application.service.ApplicationService;
import org.bson.types.Binary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ApplicationServiceTest {

    @Mock private CompanyClient companyClient;
    @Mock private ApplicationRepository applicationRepository;
    @Mock private UserClient userClient;
    @Mock private JobClient jobClient;
    @Mock private PdfRepository pdfRepository;

    @InjectMocks private ApplicationService applicationService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testListCompanyApplications_success() {
        // Arrange
        Long hrUserId = 10L;
        Long companyId = 100L;
        Long jobId = 1L;

        Application app = new Application();
        app.setId(1L);
        app.setJobId(jobId);
        app.setUserId(2L);
        app.setCompanyId(companyId);
        app.setStatus(0);
        app.setAppliedAt(LocalDateTime.now());

        Page<Application> mockPage = new PageImpl<>(List.of(app));
        when(companyClient.findCompanyIdByHr(hrUserId)).thenReturn(companyId);
        when(applicationRepository.searchByCompany(eq(companyId), eq(jobId), any(), any())).thenReturn(mockPage);

        // Mock job & user data
        JobClient.JobBrief jobBrief = new JobClient.JobBrief(1L, "Developer",1L);
        UserClient.UserBrief userBrief = new UserClient.UserBrief(2L, "Alice");

        when(jobClient.batchBrief(any())).thenReturn(List.of(jobBrief));
        when(userClient.batchBrief(any())).thenReturn(List.of(userBrief));

        // Act
        Page<ApplicationBriefResponse> result = applicationService.listCompanyApplications(hrUserId, companyId, jobId, null, PageRequest.of(0, 10));

        // Assert
        assertThat(result.getTotalElements()).isEqualTo(1);
        ApplicationBriefResponse res = result.getContent().get(0);
        assertThat(res.getJobTitle()).isEqualTo("Developer");
        assertThat(res.getApplicantName()).isEqualTo("Alice");
    }

    @Test
    void testApply_success() throws IOException {
        Long jobId = 1L, userId = 2L;
        ApplicationDTO form = new ApplicationDTO();
        form.setResumeProfile("Experienced Java Developer");

        JobSummaryDTO jobSummary = new JobSummaryDTO();
        jobSummary.setCompanyId(10L);
        jobSummary.setStatus(0);
        when(jobClient.getSummary(jobId)).thenReturn(jobSummary);
        when(applicationRepository.existsByJobIdAndUserId(jobId, userId)).thenReturn(false);

        MockMultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", "PDFDATA".getBytes());
        when(pdfRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(applicationRepository.save(any())).thenAnswer(invocation -> {
            Application saved = invocation.getArgument(0);
            saved.setId(123L);
            return saved;
        });
        ServletUriComponentsBuilder fakeBuilder = new ServletUriComponentsBuilder() {
            @Override
            public UriComponentsBuilder path(String path) {
                return UriComponentsBuilder.fromUriString("http://localhost");
            }
        };

        try (MockedStatic<ServletUriComponentsBuilder> mocked = Mockito.mockStatic(ServletUriComponentsBuilder.class)) {
            mocked.when(ServletUriComponentsBuilder::fromCurrentContextPath)
                    .thenReturn(fakeBuilder);

            Long id = applicationService.apply(jobId, userId, form, file);
            assertThat(id).isNotNull();
        }

    }

    @Test
    void testApply_duplicate_shouldThrow() {
        Long jobId = 1L, userId = 2L;

        JobSummaryDTO job = new JobSummaryDTO();
        job.setCompanyId(10L);
        job.setStatus(0);
        when(jobClient.getSummary(jobId)).thenReturn(job);

        when(applicationRepository.existsByJobIdAndUserId(jobId, userId)).thenReturn(true);

        Throwable thrown = catchThrowable(() ->
                applicationService.apply(jobId, userId, new ApplicationDTO(), null)
        );

        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Already applied");
    }


    @Test
    void testValidateFile_rejectsTooLarge() {
        Long jobId = 1L, userId = 2L;

        JobSummaryDTO job = new JobSummaryDTO();
        job.setCompanyId(10L);
        job.setStatus(0);
        when(jobClient.getSummary(anyLong())).thenReturn(job);

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", new byte[6 * 1024 * 1024]);

        Throwable thrown = catchThrowable(() ->
                applicationService.apply(jobId, userId, new ApplicationDTO(), file)
        );

        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File too large");
    }

}