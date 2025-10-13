package com.jobspring.job.api;

import com.jobspring.job.dto.JobCreateRequest;
import com.jobspring.job.dto.JobDTO;
import com.jobspring.job.client.AuthClient;
import com.jobspring.job.dto.JobResponse;
import com.jobspring.job.dto.JobSummaryResponse;
import com.jobspring.job.entity.Job;
import com.jobspring.job.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class JobController {
    private final AuthClient authClient;

    private final JobService jobService;

    @GetMapping("/test")
    public String test() {
        return "job-service-ok";
    }

    @GetMapping("/ping-auth")
    public String callAuth() {
        String result = authClient.pingFromAuth();
        return "job-service → " + result;
    }

    // 获取职位列表
    @GetMapping("/job_list")
    public Page<JobDTO> getJobList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "postedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return jobService.getJobSeekerJobs(pageable);
    }


    // 搜索职位
    @GetMapping("/job_list/search")
    public Page<JobDTO> searchJobs(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("postedAt").descending());
        return jobService.searchJobSeekerJobs(keyword, pageable);
    }

    // 供 user-service 调用：校验职位是否存在
    @GetMapping("/{jobId}/exists")
    public ResponseEntity<Boolean> exists(@PathVariable Long jobId) {
        return ResponseEntity.ok(jobService.exists(jobId));
    }

    // 供 user-service 调用：获取摘要（含公司名）
    @GetMapping("/{jobId}/summary")
    public ResponseEntity<JobSummaryResponse> summary(@PathVariable Long jobId) {
        return ResponseEntity.ok(jobService.getSummary(jobId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/status")
    public List<Map<String, Object>> getAllJobStatus() {
        return jobService.listStatus();
    }

    // 下线岗位（快捷端点，可选）
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/companies/{companyId}/jobs/{jobId}/invalid")
    public ResponseEntity<Void> deactivate(@PathVariable Long companyId,
                                           @PathVariable Long jobId) {
        jobService.deactivateJob(companyId, jobId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public JobDTO getJobById(@PathVariable Long id) {
        return jobService.getJobById(id);
    }

    @PostMapping("/companies/{companyId}")
    public ResponseEntity<JobResponse> createJob(
            @PathVariable Long companyId,
            @Valid @RequestBody JobCreateRequest req) {

        JobResponse res = jobService.createJob(companyId, req);
        return ResponseEntity.ok(res);
    }

}