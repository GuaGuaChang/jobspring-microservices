package com.jobspring.application.dto;

import lombok.Data;

@Data
public class JobSummaryDTO {
    private Long id;
    private Long companyId;
    private Integer status;
    private String title;
}