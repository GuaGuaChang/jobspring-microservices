package com.jobspring.auth.dto;

import lombok.Data;

@Data
public class CompanyView {
    private Long id;
    private String name;
    private String website;
    private Integer size;
    private String logoUrl;
    private String description;
    private String createdBy;
}
