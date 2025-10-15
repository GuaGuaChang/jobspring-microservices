package com.jobspring.user.controller;

import com.jobspring.user.client.CompanyClient;
import com.jobspring.user.client.JobClient;
import com.jobspring.user.dto.JobResponse;
import com.jobspring.user.service.HrCompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hr")
@RequiredArgsConstructor
public class HrController {
    private final HrCompanyService hrCompanyService;

    private final JobClient jobClient;

    private final CompanyClient companyClient;

    @PreAuthorize("hasRole('HR')")
    @GetMapping("/company-name")
    public ResponseEntity<String> getMyCompanyName(@RequestHeader("X-User-Id") Long userId) {
        String companyName = hrCompanyService.getMyCompanyName(userId);
        return ResponseEntity.ok(companyName);
    }

    @PreAuthorize("hasRole('HR')")
    @GetMapping("/jobs-detail/{jobId}")
    public ResponseEntity<JobResponse> getJobDetailForEdit(
            @PathVariable Long jobId,
            Authentication auth) {

        Long userId = Long.valueOf(auth.getName());

        Long companyId = companyClient.findCompanyIdByHr(userId);

        JobResponse job = jobClient.getJobForEdit(companyId, jobId);

        return ResponseEntity.ok(job);
    }
}
