package com.jobspring.user.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProfileRequestDTO {
    private ProfileDTO profile;
    private List<EducationDTO> education;
    private List<ExperienceDTO> experience;
    private List<UserSkillDTO> skills;
}
