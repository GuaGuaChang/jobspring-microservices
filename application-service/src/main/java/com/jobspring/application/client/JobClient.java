package com.jobspring.application.client;

import com.jobspring.application.dto.JobDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "job-service")
public interface JobClient {
    @GetMapping("/{id}")
    JobDTO getJobById(@PathVariable("id") Long id);

    @GetMapping("/{jobId}/brief")
    JobBrief getJobBrief(@PathVariable Long jobId);

    @PostMapping("/briefs:batch")
    List<JobBrief> batchBrief(@RequestBody List<Long> jobIds);

    record JobBrief(Long id, String title, Long companyId) {
    }
}

