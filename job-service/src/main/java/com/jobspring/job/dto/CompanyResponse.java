package com.jobspring.job.dto;

import lombok.Data;

@Data
public class CompanyResponse {
    private Long id;
    private String name;
    private String logoUrl;
    private String description;
}

