package com.jobspring.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private UserDTO user;
    private String token;
    private long expiresAt;
}