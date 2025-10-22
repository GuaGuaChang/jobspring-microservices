package com.jobspring.job.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class JobUpdateRequest {
    private String title;
    private String location;
    private String employmentType;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String description;
}