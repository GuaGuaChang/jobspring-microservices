package com.jobspring.company.dto;


public class CompanyIdResponse {
    private Long companyId;

    public CompanyIdResponse() {}
    public CompanyIdResponse(Long companyId) { this.companyId = companyId; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
}