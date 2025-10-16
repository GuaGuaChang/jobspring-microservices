package com.jobspring.application.dto;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class ApplicationDTO {

    @Column(length = 5000)
    private String resumeProfile;

}
