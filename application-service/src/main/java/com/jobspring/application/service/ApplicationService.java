package com.jobspring.application.service;

import com.jobspring.application.client.JobClient;
import com.jobspring.application.dto.ApplicationBriefResponse;
import com.jobspring.application.dto.JobDTO;
import com.jobspring.application.entity.Application;
import com.jobspring.application.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository repository;
    private final JobClient jobClient;

    @Transactional(readOnly = true)
    public Page<ApplicationBriefResponse> listMine(Long userId, Integer status, Pageable pageable) {
        Page<Application> page = (status == null)
                ? repository.findMyApplications(userId, pageable)
                : repository.findMyApplicationsByStatus(userId, status, pageable);

        return page.map(this::toBrief);
    }

    private ApplicationBriefResponse toBrief(Application a) {
        ApplicationBriefResponse dto = new ApplicationBriefResponse();
        dto.setId(a.getId());
        dto.setStatus(a.getStatus());
        dto.setAppliedAt(a.getAppliedAt());
        dto.setResumeUrl(a.getResumeUrl());
        dto.setApplicantId(a.getUserId());

        // Feign 调用 job-service
        JobDTO job = jobClient.getJobById(a.getJobId());
        if (job != null) {
            dto.setJobId(job.getId());
            dto.setJobTitle(job.getTitle());
            dto.setCompanyId(job.getId());
            dto.setCompanyName(job.getCompany());
        }

        return dto;
    }
}
