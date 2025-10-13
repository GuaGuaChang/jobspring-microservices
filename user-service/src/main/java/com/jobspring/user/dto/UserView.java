package com.jobspring.user.dto;

import lombok.Data;

@Data
public class UserView {
    private Long id;
    private Boolean active;
    private String role;
    private Long companyId;
}
