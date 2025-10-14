package com.jobspring.user.client;

import com.jobspring.user.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "job-service")
public interface JobClient {

    @GetMapping("/{jobId}/exists")
    Boolean existsById(@PathVariable("jobId") Long jobId);

    @GetMapping("/{jobId}/summary")
    JobSummaryResponse getJobSummary(@PathVariable("jobId") Long jobId);

    @PostMapping("/companies/{companyId}")
    JobResponse createJob(@PathVariable("companyId") Long companyId,
                          @RequestBody JobCreateRequest req);

    @PostMapping("/companies/{companyId}/jobs/{jobId}")
    JobResponse updateJob(
            @PathVariable("companyId") Long companyId,
            @PathVariable("jobId") Long jobId,
            @RequestBody JobUpdateRequest req);

    @GetMapping("/skills")
    List<SkillDTO> getAllSkills();
}