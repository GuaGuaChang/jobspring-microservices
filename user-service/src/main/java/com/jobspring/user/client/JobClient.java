package com.jobspring.user.client;

import com.jobspring.user.dto.JobCreateRequest;
import com.jobspring.user.dto.JobResponse;
import com.jobspring.user.dto.JobSummaryResponse;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;

@FeignClient(name = "job-service")
public interface JobClient {

    @GetMapping("/{jobId}/exists")
    Boolean existsById(@PathVariable("jobId") Long jobId);

    @GetMapping("/{jobId}/summary")
    JobSummaryResponse getJobSummary(@PathVariable("jobId") Long jobId);

    @PostMapping("/companies/{companyId}")
    JobResponse createJob(@PathVariable("companyId") Long companyId,
                          @RequestBody JobCreateRequest req);
}