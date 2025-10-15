package com.jobspring.user.api;

import com.jobspring.user.client.AuthUserClient;
import com.jobspring.user.dto.*;
import com.jobspring.user.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private AuthUserClient authUserClient;

    @PreAuthorize("hasRole('CANDIDATE')")
    @GetMapping
    public ProfileResponseDTO getMyProfile(
            @RequestHeader(value = "X-User-Id", required = false) String uidHeader) {

        System.out.println(">>> X-User-Id=" + uidHeader);
        if (uidHeader == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing X-User-Id header");

        UserView user;
        try {
            user = authUserClient.getCurrentUser(uidHeader);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Auth-service validation failed: " + e.getMessage());
        }

        if (user == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found in auth-service");

        return profileService.getCompleteProfile(user.getId());
    }

    @PreAuthorize("hasRole('CANDIDATE')")
    @PostMapping
    public ProfileUpdateResponseDTO createOrUpdateProfile(
            @RequestHeader(value = "X-User-Id", required = false) String uidHeader,
            @RequestBody ProfileRequestDTO request) {

        if (uidHeader == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing X-User-Id header");

        UserView user;
        try {
            user = authUserClient.getCurrentUser(uidHeader);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Auth-service validation failed: " + e.getMessage());
        }

        if (user == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid user ID");

        return profileService.createOrUpdateProfile(user.getId(), request);
    }
}