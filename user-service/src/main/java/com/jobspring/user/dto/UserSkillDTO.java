package com.jobspring.user.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserSkillDTO {
    private Long skillId;
    private String skillName;
    private String category;
    private Integer level;
    private Double years;
}
