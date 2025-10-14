package com.jobspring.user.controller;

import com.jobspring.user.service.HrCompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hr")
@RequiredArgsConstructor
public class HrController {
    private final HrCompanyService hrCompanyService;

    @PreAuthorize("hasRole('HR')")
    @GetMapping("/company-name")
    public ResponseEntity<String> getMyCompanyName(@RequestHeader("X-User-Id") Long userId) {
        String companyName = hrCompanyService.getMyCompanyName(userId);
        return ResponseEntity.ok(companyName);
    }
}
