package com.jobspring.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private Long id;
    private String email;
    private String fullName;
    private Byte role;
    private String token;
    private long expiresAt;
}