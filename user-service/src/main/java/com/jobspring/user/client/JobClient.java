package com.jobspring.user.client;

import com.jobspring.user.dto.JobSummaryResponse;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;

@FeignClient(name = "job-service")
public interface JobClient {

    @GetMapping("/{jobId}/exists")
    Boolean existsById(@PathVariable("jobId") Long jobId);

    @GetMapping("/{jobId}/summary")
    JobSummaryResponse getJobSummary(@PathVariable("jobId") Long jobId);

}