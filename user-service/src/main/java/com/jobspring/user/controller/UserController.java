package com.jobspring.user.controller;

import com.jobspring.user.client.JobClient;
import com.jobspring.user.dto.*;
import com.jobspring.user.client.AuthUserClient;
import com.jobspring.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final AuthUserClient authUserClient;

    private final JobClient jobClient;


    @PostMapping("/{userId}/make-hr")
    public ResponseEntity<Void> makeHr(@PathVariable("userId") Long userId,
                                       @RequestBody(required = false) PromoteToHrRequest req) {
        userService.makeHr(userId, req);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search_user")
    public ResponseEntity<Page<UserDTO>> searchUsers(
            @RequestParam(required = false, name = "q") String q,
            Pageable pageable) {

        List<String> sortParams = pageable.getSort().stream()
                .map(o -> o.getProperty() + "," + o.getDirection())
                .toList();
        List<String> sortOrNull = sortParams.isEmpty() ? null : sortParams;

        PageResponse<UserDTO> resp = authUserClient.search(
                q, pageable.getPageNumber(), pageable.getPageSize(), sortOrNull
        );

        Page<UserDTO> page = new PageImpl<>(
                resp.getContent(),
                PageRequest.of(resp.getPage(), resp.getSize(), pageable.getSort()),
                resp.getTotalElements()
        );
        return ResponseEntity.ok(page);
    }

    @PreAuthorize("hasRole('HR')")
    @PostMapping("/companies/{companyId}/jobs")
    public ResponseEntity<JobResponse> createJob(
            @PathVariable Long companyId,
            @Valid @RequestBody JobCreateRequest req) {

        JobResponse res = jobClient.createJob(companyId, req);
        return ResponseEntity.ok(res);
    }

    @PreAuthorize("hasRole('HR')")
    @PostMapping("/companies/{companyId}/jobs/{jobId}")
    public ResponseEntity<JobResponse> updateJob(
            @PathVariable Long companyId,
            @PathVariable Long jobId,
            @RequestBody JobUpdateRequest req) {
        JobResponse res = jobClient.updateJob(companyId, jobId, req);
        return ResponseEntity.ok(res);
    }
    @PostMapping("/briefs:batch")
    public List<UserBrief> batchBrief(@RequestBody List<Long> userIds) {
        return userService.batchBrief(userIds);
    }

}
