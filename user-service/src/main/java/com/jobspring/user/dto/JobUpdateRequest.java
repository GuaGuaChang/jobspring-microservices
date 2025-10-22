package com.jobspring.user.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class JobUpdateRequest {
    private String title;
    private String location;
    private Integer employmentType;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String description;
}