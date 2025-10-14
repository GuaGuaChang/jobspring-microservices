package com.jobspring.company.client;

import com.jobspring.company.dto.JobResponse;
import com.jobspring.company.dto.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "job-service")
public interface JobClient {

    @GetMapping("/company/{companyId}")
    PageResponse<JobResponse> getCompanyJobs(
            @PathVariable("companyId") Long companyId,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam("page") int page,
            @RequestParam("size") int size
    );
}