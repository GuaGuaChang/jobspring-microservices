package com.jobspring.job.service;

import com.jobspring.job.client.CompanyClient;
import com.jobspring.job.dto.JobDTO;
import com.jobspring.job.dto.JobDeactivatedEvent;
import com.jobspring.job.dto.JobSummaryResponse;
import com.jobspring.job.entity.Job;
import com.jobspring.job.repository.JobRepository;
import com.jobspring.job.repository.SkillRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.isA;


@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock JobRepository jobRepository;
    @Mock SkillRepository skillRepository;
    @Mock CompanyClient companyClient;
    @Mock ApplicationEventPublisher publisher;

    @InjectMocks JobService jobService;

    private Job newJob() {
        Job j = new Job();
        j.setId(100L);
        j.setCompanyId(7L);
        j.setTitle("Java Developer");
        j.setLocation("Singapore");
        j.setEmploymentType(1);
        j.setSalaryMin(new BigDecimal("8000"));
        j.setSalaryMax(new BigDecimal("12000"));
        j.setDescription("Backend dev");
        j.setStatus(0);
        j.setPostedAt(LocalDateTime.now());
        return j;
    }

    @Test
    void getJobSeekerJobs_should_map_basic_fields_and_tags_and_company() {
        Job j = newJob();
        Pageable pageable = PageRequest.of(0, 10, Sort.by("postedAt").descending());
        when(jobRepository.findByStatus(eq(0), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(j), pageable, 1));
        when(skillRepository.findSkillNamesByJobId(100L))
                .thenReturn(List.of("Java", "Spring"));
        // companyClient.getCompanyById 用于补公司名
        var company = new com.jobspring.job.dto.CompanyResponse();
        company.setId(7L); company.setName("ACME");
        when(companyClient.getCompanyById(7L)).thenReturn(company);

        Page<JobDTO> page = jobService.getJobSeekerJobs(pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
        JobDTO dto = page.getContent().get(0);
        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getTitle()).isEqualTo("Java Developer");
        assertThat(dto.getCompanyId()).isEqualTo(7L);
        assertThat(dto.getCompany()).isEqualTo("ACME");
        assertThat(dto.getLocation()).isEqualTo("Singapore");
        assertThat(dto.getEmploymentType()).isEqualTo("Full Time");
        assertThat(dto.getSalaryMin()).isEqualByComparingTo("8000");
        assertThat(dto.getSalaryMax()).isEqualByComparingTo("12000");
        assertThat(dto.getDescription()).isEqualTo("Backend dev");
        assertThat(dto.getTags()).containsExactly("Java", "Spring");
    }

    @Test
    void searchJobSeekerJobs_should_use_companyIds_and_map() {
        Job j = newJob();
        Pageable pageable = PageRequest.of(0, 10);
        when(companyClient.findCompanyIdsByName("ac"))
                .thenReturn(List.of(7L, 8L));
        when(jobRepository.searchJobs(eq("ac"), eq(List.of(7L,8L)), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(j), pageable, 1));
        when(skillRepository.findSkillNamesByJobId(100L))
                .thenReturn(List.of("Java"));

        Page<JobDTO> page = jobService.searchJobSeekerJobs("ac", pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getTags()).containsExactly("Java");
    }

    @Test
    void getSummary_should_fill_companyName_if_company_exists() {
        Job j = newJob();
        when(jobRepository.findById(100L)).thenReturn(Optional.of(j));

        var company = new com.jobspring.job.dto.CompanyResponse();
        company.setId(7L); company.setName("ACME");
        when(companyClient.getCompanyById(7L)).thenReturn(company);

        JobSummaryResponse r = jobService.getSummary(100L);

        assertThat(r.getId()).isEqualTo(100L);
        assertThat(r.getCompanyName()).isEqualTo("ACME");
        assertThat(r.getStatus()).isEqualTo(0);
        assertThat(r.getEmploymentType()).isEqualTo(1);
    }

    @Test
    void listStatus_should_join_company_name() {
        Job j1 = newJob();
        Job j2 = newJob(); j2.setId(101L);

        when(jobRepository.findAll()).thenReturn(List.of(j1, j2));

        var dto = new com.jobspring.job.dto.CompanyDTO();
        dto.setId(7L);
        dto.setName("ACME");
        var map = Map.of(7L, dto);

        when(companyClient.findByIds(List.of(7L))).thenReturn(map);

        var list = jobService.listStatus();

        assertThat(list).hasSize(2);
        assertThat(list.get(0)).containsEntry("company", "ACME");
    }

    @Test
    void deactivateJob_should_set_status_and_publish_event() {
        Job j = newJob();
        when(jobRepository.findByIdAndCompanyId(100L, 7L)).thenReturn(Optional.of(j));

        jobService.deactivateJob(7L, 100L);

        // 保存为 status=1
        ArgumentCaptor<Job> cap = ArgumentCaptor.forClass(Job.class);
        verify(jobRepository).save(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo(1);
        // 发布事件
        verify(publisher).publishEvent(isA(JobDeactivatedEvent.class));
    }

    @Test
    void findCompanyIdByUserId_should_throw_if_not_found() {
        when(companyClient.getCompanyIdByHr(55L)).thenReturn(null);
        assertThatThrownBy(() -> jobService.findCompanyIdByUserId(55L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void listJobs_should_choose_repo_by_status() {
        Pageable pageable = PageRequest.of(0, 5);
        Job j = newJob();

        when(jobRepository.findAllByCompanyId(7L, pageable))
                .thenReturn(new PageImpl<>(List.of(j), pageable, 1));

        var p1 = jobService.listJobs(7L, null, pageable);
        assertThat(p1.getTotalElements()).isEqualTo(1);

        when(jobRepository.findByCompanyIdAndStatus(7L, 0, pageable))
                .thenReturn(new PageImpl<>(List.of(j), pageable, 1));

        var p2 = jobService.listJobs(7L, 0, pageable);
        assertThat(p2.getTotalElements()).isEqualTo(1);
    }

    @Test
    void createJob_should_persist_and_return_response() {
        when(jobRepository.save(any(Job.class))).thenAnswer(inv -> {
            Job saved = inv.getArgument(0);
            saved.setId(999L);
            saved.setPostedAt(LocalDateTime.now());
            return saved;
        });

        var req = new com.jobspring.job.dto.JobCreateRequest();
        req.setTitle("New");
        req.setLocation("SG");
        req.setEmploymentType(2);
        req.setSalaryMin(new BigDecimal("1000"));
        req.setSalaryMax(new BigDecimal("2000"));
        req.setDescription("desc");

        var res = jobService.createJob(7L, req);

        assertThat(res.getId()).isEqualTo(999L);
        assertThat(res.getCompanyId()).isEqualTo(7L);
        assertThat(res.getTitle()).isEqualTo("New");
        assertThat(res.getEmploymentType()).isEqualTo(2);
    }

    @Test
    void updateJob_should_check_company_and_update_fields() {
        Job j = newJob(); // companyId=7L
        when(jobRepository.findById(100L)).thenReturn(Optional.of(j));
        when(jobRepository.save(any(Job.class))).thenAnswer(inv -> inv.getArgument(0));

        var req = new com.jobspring.job.dto.JobUpdateRequest();
        req.setSalaryMin(new BigDecimal("9000"));
        req.setDescription("updated");
        req.setTitle("updated");
        req.setEmploymentType(1);

        var res = jobService.updateJob(7L, 100L, req);

        assertThat(res.getSalaryMin()).isEqualByComparingTo("9000");
        assertThat(res.getDescription()).isEqualTo("updated");
    }

    @Test
    void getBrief_getCompanyId_batchBrief_should_work() {
        Job j = newJob();
        when(jobRepository.findById(100L)).thenReturn(Optional.of(j));
        when(jobRepository.findCompanyIdById(100L)).thenReturn(Optional.of(7L));
        when(jobRepository.findAllById(List.of(100L, 101L)))
                .thenReturn(List.of(j, new Job(){{
                    setId(101L); setTitle("B"); setCompanyId(8L);
                }}));

        var brief = jobService.getBrief(100L);
        assertThat(brief.id()).isEqualTo(100L);

        var cid = jobService.getCompanyId(100L);
        assertThat(cid).isEqualTo(7L);

        var briefs = jobService.batchBrief(List.of(100L, 101L));
        assertThat(briefs).hasSize(2);
    }

    @Test
    void getJobForEdit_should_throw_when_company_mismatch() {
        Job j = newJob();
        j.setCompanyId(99L);
        when(jobRepository.findById(100L)).thenReturn(Optional.of(j));

        assertThatThrownBy(() -> jobService.getJobForEdit(7L, 100L))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
    }
}
