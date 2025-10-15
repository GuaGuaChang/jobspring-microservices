package com.jobspring.application.api;

import com.jobspring.application.dto.ApplicationBriefResponse;
import com.jobspring.application.dto.ApplicationDetailResponse;
import com.jobspring.application.dto.UpdateStatusBody;
import com.jobspring.application.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @GetMapping("/getApplications")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<Page<ApplicationBriefResponse>> listMine(
            @RequestParam(required = false) Integer status,
            Pageable pageable,
            Authentication auth) {

        Long userId = Long.valueOf(auth.getName());
        return ResponseEntity.ok(applicationService.listMine(userId, status, pageable));
    }

    @GetMapping("/applications")
    public ResponseEntity<Page<ApplicationBriefResponse>> listMine(
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) Integer status,
            Pageable pageable,
            @RequestHeader("X-User-Id") Long hrUserId
    ) {
        Page<ApplicationBriefResponse> page = applicationService
                .listCompanyApplications(hrUserId, null, jobId, status, pageable);
        return ResponseEntity.ok(page);
    }
    @PreAuthorize("hasAnyRole('HR','ADMIN')")
    @GetMapping("/{applicationId}")
    public ResponseEntity<ApplicationDetailResponse> getApplicationDetail(
            @PathVariable Long applicationId,
            @RequestParam(required = false) Long companyId,
            @RequestHeader("X-User-Id") Long userId
    ) {

        var resp = applicationService.getApplicationDetail(userId, companyId, applicationId);
        return ResponseEntity.ok(resp);
    }
    @PatchMapping("/applications/{applicationId}/status")
    @PreAuthorize("hasAnyRole('HR')")
    public ResponseEntity<ApplicationBriefResponse> updateStatus(
            @PathVariable Long applicationId,
            @RequestBody UpdateStatusBody body,
            Authentication auth) {

        Long hrUserId = Long.valueOf(auth.getName());
        ApplicationBriefResponse res =
                applicationService.updateStatus(hrUserId, applicationId, body.getStatus());
        return ResponseEntity.ok(res);
    }
}
