package com.jobspring.application.service;

import com.jobspring.application.client.CompanyClient;
import com.jobspring.application.client.JobClient;
import com.jobspring.application.client.UserClient;
import com.jobspring.application.dto.*;
import com.jobspring.application.entity.Application;
import com.jobspring.application.entity.PdfFile;
import com.jobspring.application.repository.ApplicationRepository;
import com.jobspring.application.repository.PdfRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.bson.types.Binary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final CompanyClient companyClient;
    private final ApplicationRepository applicationRepository;
    private final UserClient userClient;
    private final JobClient jobClient;
    private final PdfRepository pdfRepository;

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
        r.setResumeFileId(a.getResumeFileId());
        return r;
    }

    @Transactional
    public ApplicationBriefResponse updateStatus(Long hrUserId, Long applicationId, Integer newStatus) {
        if (newStatus == null || !ALLOWED.contains(newStatus)) {
            throw new IllegalArgumentException("Illegal application status：" + newStatus);
        }

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

    //保存文件到 Mongo，返回 {publicId, url}
    public Map<String, String> store(MultipartFile file) throws IOException {
        String publicId = UUID.randomUUID().toString().replace("-", "");

        PdfFile pdf = PdfFile.builder()
                .publicId(publicId)
                .filename(file.getOriginalFilename())
                .contentType(file.getContentType())
                .data(new Binary(file.getBytes()))
                .uploadAt(Instant.now())
                .build();

        pdfRepository.save(pdf);

        UriComponentsBuilder b = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/files/download/{pid}");
        String url = b.buildAndExpand(publicId).toUriString();

        return Map.of("publicId", publicId, "url", url);
    }

    @Transactional
    public Long apply(Long jobId, Long userId, ApplicationDTO form, MultipartFile file) {
        // 1) 远程校验岗位
        JobSummaryDTO job = jobClient.getSummary(jobId);
        if (job.getStatus() != 0) {
            throw new IllegalStateException("Job inactive");
        }

        // 2) 防重复
        if (applicationRepository.existsByJobIdAndUserId(jobId, userId)) {
            throw new IllegalArgumentException("Already applied");
        }

        // 3) 处理简历：保存到 Mongo
        String publicId = null;

        validateFile(file);
        try {
            publicId = store(file).get("publicId");
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save file", e);
        }


        // 4) 入库
        Application app = new Application();
        app.setJobId(jobId);
        app.setUserId(userId);
        app.setCompanyId(job.getCompanyId());
        app.setStatus(0);
        app.setAppliedAt(LocalDateTime.now());
        /*app.setResumeProfile(form.getResumeProfile());*/
        app.setResumeProfile(null);
        app.setResumeFileId(publicId);

        applicationRepository.save(app);
        return app.getId();
    }

    private void validateFile(MultipartFile file) {
        if (file.getSize() > 5 * 1024 * 1024) throw new IllegalArgumentException("File too large");
        if (!"application/pdf".equalsIgnoreCase(
                Optional.ofNullable(file.getContentType()).orElse(""))) {
            throw new IllegalArgumentException("Only PDF allowed");
        }
    }
}
