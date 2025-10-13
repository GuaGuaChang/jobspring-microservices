package com.jobspring.application.client;

import com.jobspring.application.dto.JobDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "job-service")
public interface JobClient {
    @GetMapping("/{id}")
    JobDTO getJobById(@PathVariable("id") Long id);
}

