package com.jobspring.auth.dto;

import lombok.Data;

@Data
public class PromoteToHrRequest {
    private Long companyId;

    private Boolean overwriteCompany = true;
}
