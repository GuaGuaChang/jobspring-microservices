package com.jobspring.user.dto;

import lombok.Data;

@Data
public class ProfileDTO {
    private String summary;
    private Integer visibility;
    private String fileUrl;
}
