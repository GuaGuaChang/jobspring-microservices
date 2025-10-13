package com.jobspring.user.dto;

import lombok.Data;

@Data
public class PromoteToHrRequest {
    private Long companyId;

    private Boolean overwriteCompany = true;
}
