package com.jobspring.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JobCreateRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String location;
    @NotNull
    private Integer employmentType;
    @NotNull
    private Double salaryMin;
    @NotNull
    private Double salaryMax;
    @NotBlank
    private String description;
}
