package com.jobspring.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String email;
    private String phone;
    private String fullName;
    private Integer role;
    private Boolean isActive;
    private Long companyId;

    public UserDTO(Long id, String email, String fullName, Integer role) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
    }
}