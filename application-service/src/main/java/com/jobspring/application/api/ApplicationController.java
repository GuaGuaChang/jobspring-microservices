package com.jobspring.application.api;

import com.jobspring.application.dto.ApplicationBriefResponse;
import com.jobspring.application.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService service;

    @GetMapping("/getApplications")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<Page<ApplicationBriefResponse>> listMine(
            @RequestParam(required = false) Integer status,
            Pageable pageable,
            Authentication auth) {

        Long userId = Long.valueOf(auth.getName());
        return ResponseEntity.ok(service.listMine(userId, status, pageable));
    }
}
