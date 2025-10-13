package com.jobspring.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProfileUpdateResponseDTO {
    private String status;
    private String message;
    private Long profileId;
}
