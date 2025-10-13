package com.jobspring.user.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JobResponse {
    private Long id;
    private Long companyId;
    private String title;
    private String location;
    private Integer employmentType;
    private Double salaryMin;
    private Double salaryMax;
    private String description;
    private Integer status;
    private LocalDateTime postedAt;
}
