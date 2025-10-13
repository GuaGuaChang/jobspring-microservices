package com.jobspring.job.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class JobCreateRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String location;
    @NotNull
    private Integer employmentType;
    @NotNull
    private BigDecimal salaryMin;
    @NotNull
    private BigDecimal salaryMax;
    @NotBlank
    private String description;
}
