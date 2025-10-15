package com.jobspring.application.service;

import com.jobspring.application.client.CompanyClient;
import com.jobspring.application.client.JobClient;
import com.jobspring.application.client.UserClient;
import com.jobspring.application.dto.ApplicationBriefResponse;
import com.jobspring.application.dto.ApplicationDetailResponse;
import com.jobspring.application.dto.JobDTO;
import com.jobspring.application.dto.UserDTO;
import com.jobspring.application.entity.Application;
import com.jobspring.application.repository.ApplicationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final CompanyClient companyClient;
    private final ApplicationRepository applicationRepository;
    private final UserClient userClient;
    private final JobClient jobClient;
    private static final Set<Integer> ALLOWED =
            Set.of(0, 1, 2, 3, 4);

    public Page<ApplicationBriefResponse> listCompanyApplications(
            Long hrUserId,
            Long companyId,
            Long jobId,
            Integer status,
            Pageable pageable
    ) {
        final Long effectiveCompanyId = (companyId == null)
                ? companyClient.findCompanyIdByHr(hrUserId)
                : validateAndReturn(hrUserId, companyId);


        Page<Application> page = applicationRepository.searchByCompany(effectiveCompanyId, jobId, status, pageable);
        List<Long> jobIds = page.stream().map(Application::getJobId).distinct().toList();
        List<Long> userIds = page.stream().map(Application::getUserId).distinct().toList();
        Map<Long, JobClient.JobBrief> jobMap = jobClient.batchBrief(jobIds).stream()
                .collect(Collectors.toMap(JobClient.JobBrief::id, x -> x));
        Map<Long, UserClient.UserBrief> userMap = userClient.batchBrief(userIds).stream()
                .collect(Collectors.toMap(UserClient.UserBrief::id, x -> x));
        return page.map(a -> toBrief(a, jobMap, userMap));
    }

    private Long validateAndReturn(Long hrUserId, Long companyId) {
        companyClient.assertHrInCompany(hrUserId, companyId);
        return companyId;
    }

    private ApplicationBriefResponse toBrief(
            Application a,
            Map<Long, JobClient.JobBrief> jobMap,
            Map<Long, UserClient.UserBrief> userMap) {

        JobClient.JobBrief jb = jobMap.get(a.getJobId());
        UserClient.UserBrief ub = userMap.get(a.getUserId());

        ApplicationBriefResponse r = new ApplicationBriefResponse();
        r.setId(a.getId());
        r.setJobId(a.getJobId());
        r.setJobTitle(jb != null ? jb.title() : "(unknown)");
        r.setApplicantId(a.getUserId());
        r.setApplicantName(ub != null ? ub.fullName() : "(unknown)");
        r.setStatus(a.getStatus());
        r.setAppliedAt(a.getAppliedAt());
        r.setResumeUrl(a.getResumeUrl());
        return r;
    }

    @Transactional(readOnly = true)
    public Page<ApplicationBriefResponse> listMine(Long userId, Integer status, Pageable pageable) {
        Page<Application> page = (status == null)
                ? applicationRepository.findMyApplications(userId, pageable)
                : applicationRepository.findMyApplicationsByStatus(userId, status, pageable);

        return page.map(this::toBrief);
    }

    private ApplicationBriefResponse toBrief(Application a) {
        ApplicationBriefResponse dto = new ApplicationBriefResponse();
        dto.setId(a.getId());
        dto.setStatus(a.getStatus());
        dto.setAppliedAt(a.getAppliedAt());
        dto.setResumeUrl(a.getResumeUrl());
        dto.setApplicantId(a.getUserId());

        JobDTO job = jobClient.getJobById(a.getJobId());
        if (job != null) {
            dto.setJobId(job.getId());
            dto.setJobTitle(job.getTitle());
            dto.setCompanyId(job.getId());
            dto.setCompanyName(job.getCompany());
        }
        return dto;
    }

    public ApplicationDetailResponse getApplicationDetail(Long hrUserId, Long companyId, Long applicationId) {
        Long effectiveCompanyId = (companyId == null)
                ? companyClient.findCompanyIdByHr(hrUserId)
                : validateAndReturn(hrUserId, companyId);

        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new EntityNotFoundException("Application not found"));

        Long jobCompanyId = app.getCompanyId();
        if (!jobCompanyId.equals(effectiveCompanyId)) {
            throw new SecurityException("You are not allowed to access this application");
        }

        return toDetail(app);
    }
    private ApplicationDetailResponse toDetail(Application a) {
        ApplicationDetailResponse r = new ApplicationDetailResponse();
        JobDTO job = jobClient.getJobById(a.getJobId());
        UserDTO user = userClient.getUserById(a.getUserId());
        r.setId(a.getId());
        r.setJobId(a.getJobId());
        r.setJobTitle(job.getTitle());
        r.setApplicantId(a.getUserId());
        r.setApplicantName(user.getFullName());
        r.setApplicantEmail(user.getEmail());
        r.setStatus(a.getStatus());
        r.setAppliedAt(a.getAppliedAt());
        r.setResumeUrl(a.getResumeUrl());
        r.setResumeProfile(a.getResumeProfile());
        return r;
    }
    @Transactional
    public ApplicationBriefResponse updateStatus(Long hrUserId, Long applicationId, Integer newStatus) {
        if (newStatus == null || !ALLOWED.contains(newStatus)) {
            throw new IllegalArgumentException("Illegal application status：" + newStatus);
        }

        // 取出申请 + 关联的 job & company
        Application app = applicationRepository.findByIdWithJobAndCompany(applicationId)
                .orElseThrow(() -> new EntityNotFoundException("Application not found"));


        Long hrCompanyId = companyClient.findCompanyIdByHr(hrUserId);
        Long appCompanyId = app.getCompanyId();
        if (!appCompanyId.equals(hrCompanyId)) {
            throw new AccessDeniedException("No permission to operate applications from other companies");
        }

        if (app.getStatus() != 0) {
            throw new IllegalStateException("This position is no longer valid and the application status cannot be modified");
        }


        app.setStatus(newStatus);

        return toBrief(app);
    }
}
