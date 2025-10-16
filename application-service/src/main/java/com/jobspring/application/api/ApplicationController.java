package com.jobspring.application.api;

import com.jobspring.application.dto.ApplicationBriefResponse;
import com.jobspring.application.dto.ApplicationDTO;
import com.jobspring.application.dto.ApplicationDetailResponse;
import com.jobspring.application.dto.UpdateStatusBody;
import com.jobspring.application.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;

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

    @PostMapping("/applications/{applicationId}/status")
    public ResponseEntity<ApplicationBriefResponse> updateStatus(
            @PathVariable Long applicationId,
            @RequestBody UpdateStatusBody body,
            @RequestHeader("X-User-Id") Long hrUserId) {

        ApplicationBriefResponse res =
                applicationService.updateStatus(hrUserId, applicationId, body.getStatus());
        return ResponseEntity.ok(res);
    }

    @PostMapping(value = "/applications/{jobId}/applications", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> apply(@PathVariable Long jobId,
                                      @ModelAttribute ApplicationDTO form,
                                      @RequestPart(value = "file", required = false) MultipartFile file,
                                      Authentication auth) {
        Long userId = Long.valueOf(auth.getName());
        Long id = applicationService.apply(jobId, userId, form, file);
        return ResponseEntity.created(URI.create("/api/applications/" + id)).build();
    }
}
