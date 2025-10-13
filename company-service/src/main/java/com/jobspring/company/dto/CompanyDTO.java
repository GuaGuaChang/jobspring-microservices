package com.jobspring.company.dto;


import jakarta.persistence.Column;
import lombok.Data;


@Data
public class CompanyDTO {
    private Long id;
    private String name;
    private String website;
    private Integer size;
    private String logoUrl;
    private String description;
    private String createdBy;

    public CompanyDTO() {

    }
    public CompanyDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
