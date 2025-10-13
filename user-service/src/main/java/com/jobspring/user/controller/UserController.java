package com.jobspring.user.controller;

import com.jobspring.user.client.JobClient;
import com.jobspring.user.dto.JobCreateRequest;
import com.jobspring.user.dto.JobResponse;
import com.jobspring.user.dto.PromoteToHrRequest;
import com.jobspring.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    private final JobClient jobClient;


    @PostMapping("/{userId}/make-hr")
    public ResponseEntity<Void> makeHr(@PathVariable("userId") Long userId,
                                       @RequestBody(required = false) PromoteToHrRequest req) {
        userService.makeHr(userId, req);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('HR')")
    @PostMapping("/companies/{companyId}/jobs")
    public ResponseEntity<JobResponse> createJob(
            @PathVariable Long companyId,
            @Valid @RequestBody JobCreateRequest req) {

        JobResponse res = jobClient.createJob(companyId, req);
        return ResponseEntity.ok(res);
    }

}
