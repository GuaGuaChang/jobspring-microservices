package com.jobspring.user.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class JobUpdateRequest {
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String description;
}