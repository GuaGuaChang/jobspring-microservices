package com.jobspring.user.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ExperienceDTO {
    private String company;
    private String title;
    private String startDate;
    private String endDate;
    private String description;
    private String achievements;
}
