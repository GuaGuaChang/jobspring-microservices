package com.jobspring.job.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JobSummaryResponse {
    private Long id;
    private String title;
    private String location;
    private Integer employmentType;
    private Integer status;
    private String companyName;   // 通过 CompanyClient 查到
    private LocalDateTime postedAt;
}