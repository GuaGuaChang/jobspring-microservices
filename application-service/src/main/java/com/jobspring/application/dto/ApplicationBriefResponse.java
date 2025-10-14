package com.jobspring.application.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApplicationBriefResponse {
    private Long id;
    private Long jobId;
    private String jobTitle;
    private Long applicantId;
    private String applicantName;
    private Integer status;
    private LocalDateTime appliedAt;
    private String resumeUrl;
    private Long companyId;
    private String companyName;
}
