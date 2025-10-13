package com.jobspring.job.service;

import com.jobspring.job.client.CompanyClient;
import com.jobspring.job.dto.*;
import com.jobspring.job.entity.Job;
import com.jobspring.job.repository.JobRepository;
import com.jobspring.job.repository.SkillRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;

    private final SkillRepository skillRepository;

    private final CompanyClient companyClient;

    private final ApplicationEventPublisher publisher;


    // 为求职者获取职位列表
    public Page<JobDTO> getJobSeekerJobs(Pageable pageable) {
        Page<Job> jobs = jobRepository.findByStatus(0, pageable);
        return jobs.map(this::convertToJobSeekerDTO);
    }

    // 转换方法
    private JobDTO convertToJobSeekerDTO(Job job) {
        JobDTO dto = new JobDTO();
        dto.setId(job.getId());
        dto.setTitle(job.getTitle());
        dto.setLocation(job.getLocation());
        dto.setSalaryMin(job.getSalaryMin());
        dto.setSalaryMax(job.getSalaryMax());
        dto.setPostedAt(job.getPostedAt());

        dto.setEmploymentType(getEmploymentTypeName(job.getEmploymentType()));

        dto.setDescription(job.getDescription());

        dto.setTags(getJobTags(job.getId()));

        // 调用 company-service 获取公司信息
        if (job.getCompanyId() != null) {
            try {
                CompanyResponse company = companyClient.getCompanyById(job.getCompanyId());
                if (company != null) {
                    dto.setCompany(company.getName());
                    dto.setCompanyId(company.getId());
                }
            } catch (Exception e) {
                // 可加日志防止外部调用失败影响主流程
                System.err.println("Failed to fetch company info for jobId " + job.getId());
            }
        }
        return dto;
    }

    // 获取工作类型名称
    private String getEmploymentTypeName(Integer type) {
        if (type == null) return "未知";
        return switch (type) {
            case 1 -> "Full-time";
            case 2 -> "Internship";
            case 3 -> "Contract";
            default -> "未知";
        };
    }

    // 获取职位标签（技能）
    private List<String> getJobTags(Long jobId) {
        return skillRepository.findSkillNamesByJobId(jobId);
    }

    // 搜索职位（求职者用）
    public Page<JobDTO> searchJobSeekerJobs(String keyword, Pageable pageable) {
        List<Long> companyIds = companyClient.findCompanyIdsByName(keyword);
        Page<Job> jobs = jobRepository.searchJobs(keyword, companyIds, pageable);
        return jobs.map(this::convertToJobSeekerDTO);
    }

    public boolean exists(Long jobId) {
        return jobRepository.existsById(jobId);
    }

    public JobSummaryResponse getSummary(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new EntityNotFoundException("Job not found"));

        JobSummaryResponse r = new JobSummaryResponse();
        r.setId(job.getId());
        r.setTitle(job.getTitle());
        r.setLocation(job.getLocation());
        r.setEmploymentType(job.getEmploymentType());
        r.setStatus(job.getStatus());
        r.setPostedAt(job.getPostedAt());

        // 补公司名（companyId 可能为空）
        if (job.getCompanyId() != null) {
            try {
                CompanyResponse c = companyClient.getCompanyById(job.getCompanyId());
                r.setCompanyName(c != null ? c.getName() : null);
            } catch (Exception ignore) {
                // 兜底：公司服务不可用时不影响主流程
                r.setCompanyName(null);
            }
        }
        return r;
    }


    public List<Map<String, Object>> listStatus() {
        List<Job> all = jobRepository.findAll();
        List<Long> companyIds = all.stream().map(Job::getCompanyId).distinct().toList();

        Map<Long, CompanyDTO> companyMap = companyClient.findByIds(companyIds);

        return all.stream().map(j -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", j.getId());
            m.put("title", j.getTitle());
            m.put("companyId", j.getCompanyId());
            m.put("company",
                    Optional.ofNullable(companyMap.get(j.getCompanyId()))
                            .map(CompanyDTO::getName)
                            .orElse("N/A"));
            m.put("status", j.getStatus());
            return m;
        }).toList();
    }

    @Transactional
    public void deactivateJob(Long companyId, Long jobId) {
        Job job = jobRepository.findByIdAndCompanyId(jobId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Job not found"));

        // 1. 下线岗位
        job.setStatus(1);
        jobRepository.save(job);

        // 2. 同步更新所有相关申请状态为 4（无效）
        publisher.publishEvent(new JobDeactivatedEvent(companyId, jobId));
        //applicationRepository.updateStatusByJobId(jobId, 4);
    }

/*    // 根据 userId 找到 HR 所属的公司
    public Long findCompanyIdByUserId(Long userId) {
        try {
            var resp = companyMemberClient.getCompanyIdByHr(userId);
            if (resp == null || resp.getCompanyId() == null) {
                throw new EntityNotFoundException("HR membership not found");
            }
            return resp.getCompanyId();
        } catch (FeignException.NotFound e) {
            throw new EntityNotFoundException("HR membership not found");
        }
    }

    // HR 查看本公司岗位（包含上下线）
    public Page<JobResponse> listJobs(Long companyId, Integer status, Pageable pageable) {
        Page<Job> page = (status == null)
                ? jobRepository.findByCompanyId(companyId, pageable)
                : jobRepository.findByCompanyIdAndStatus(companyId, status, pageable);
        return page.map(this::toResponse);
    }

    private JobResponse toResponse(Job j) {
        JobResponse r = new JobResponse();
        r.setId(j.getId());
        r.setCompanyId(j.getCompany().getId());
        r.setTitle(j.getTitle());
        r.setLocation(j.getLocation());
        r.setEmploymentType(j.getEmploymentType());
        r.setSalaryMin(j.getSalaryMin());
        r.setSalaryMax(j.getSalaryMax());
        r.setDescription(j.getDescription());
        r.setStatus(j.getStatus());
        r.setPostedAt(j.getPostedAt());
        return r;
    }*/

}
