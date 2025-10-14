package com.jobspring.user.service;

import com.jobspring.user.dto.*;
import com.jobspring.user.entity.Profile;
import com.jobspring.user.entity.ProfileEducation;
import com.jobspring.user.entity.ProfileExperience;
import com.jobspring.user.repository.ProfileEducationRepository;
import com.jobspring.user.repository.ProfileExperienceRepository;
import com.jobspring.user.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Collections;
import java.util.List;

@Service
public class ProfileService {

    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private ProfileEducationRepository educationRepository;
    @Autowired
    private ProfileExperienceRepository experienceRepository;

    public ProfileResponseDTO getCompleteProfile(Long userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        List<ProfileEducation> educations = educationRepository.findByProfileId(profile.getId());
        List<ProfileExperience> experiences = experienceRepository.findByProfileId(profile.getId());

        ProfileResponseDTO resp = new ProfileResponseDTO();
        ProfileDTO p = new ProfileDTO();
        p.setSummary(profile.getSummary());
        p.setVisibility(profile.getVisibility());
        p.setFileUrl(profile.getFileUrl());
        resp.setProfile(p);
        resp.setEducation(educations.stream().map(this::toEdu).toList());
        resp.setExperience(experiences.stream().map(this::toExp).toList());
        resp.setSkills(Collections.emptyList()); // 预留 user_skills
        return resp;
    }

    @Transactional
    public ProfileUpdateResponseDTO createOrUpdateProfile(Long userId, ProfileRequestDTO request) {
        Profile profile = profileRepository.findByUserId(userId).orElseGet(() -> {
            Profile p = new Profile();
            p.setUserId(userId);
            return p;
        });

        profile.setSummary(request.getProfile().getSummary());
        profile.setVisibility(request.getProfile().getVisibility());
        profile.setFileUrl(request.getProfile().getFileUrl());
        profileRepository.save(profile);

        educationRepository.deleteByProfileId(profile.getId());
        experienceRepository.deleteByProfileId(profile.getId());

        if (request.getEducation() != null)
            for (EducationDTO e : request.getEducation())
                saveEducation(profile.getId(), e);

        if (request.getExperience() != null)
            for (ExperienceDTO ex : request.getExperience())
                saveExperience(profile.getId(), ex);

        return new ProfileUpdateResponseDTO("success", "Profile updated successfully", profile.getId());
    }

    private void saveEducation(Long profileId, EducationDTO dto) {
        ProfileEducation e = new ProfileEducation();
        e.setProfileId(profileId);
        e.setSchool(dto.getSchool());
        e.setDegree(dto.getDegree());
        e.setMajor(dto.getMajor());
        if (dto.getStartDate() != null) e.setStartDate(Date.valueOf(dto.getStartDate()).toLocalDate());
        if (dto.getEndDate() != null) e.setEndDate(Date.valueOf(dto.getEndDate()).toLocalDate());
        if (dto.getGpa() != null) e.setGpa(BigDecimal.valueOf(dto.getGpa()));
        e.setDescription(dto.getDescription());
        educationRepository.save(e);
    }

    private void saveExperience(Long profileId, ExperienceDTO dto) {
        ProfileExperience ex = new ProfileExperience();
        ex.setProfileId(profileId);
        ex.setCompany(dto.getCompany());
        ex.setTitle(dto.getTitle());
        if (dto.getStartDate() != null) ex.setStartDate(Date.valueOf(dto.getStartDate()).toLocalDate());
        if (dto.getEndDate() != null) ex.setEndDate(Date.valueOf(dto.getEndDate()).toLocalDate());
        ex.setDescription(dto.getDescription());
        ex.setAchievements(dto.getAchievements());
        experienceRepository.save(ex);
    }

    private EducationDTO toEdu(ProfileEducation e) {
        EducationDTO dto = new EducationDTO();
        dto.setSchool(e.getSchool());
        dto.setDegree(e.getDegree());
        dto.setMajor(e.getMajor());
        dto.setStartDate(e.getStartDate() != null ? e.getStartDate().toString() : null);
        dto.setEndDate(e.getEndDate() != null ? e.getEndDate().toString() : null);
        dto.setGpa(e.getGpa() != null ? e.getGpa().doubleValue() : null);
        dto.setDescription(e.getDescription());
        return dto;
    }

    private ExperienceDTO toExp(ProfileExperience e) {
        ExperienceDTO dto = new ExperienceDTO();
        dto.setCompany(e.getCompany());
        dto.setTitle(e.getTitle());
        dto.setStartDate(e.getStartDate() != null ? e.getStartDate().toString() : null);
        dto.setEndDate(e.getEndDate() != null ? e.getEndDate().toString() : null);
        dto.setDescription(e.getDescription());
        dto.setAchievements(e.getAchievements());
        return dto;
    }

    public Profile getProfileById(Long id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
    }

    public ProfileDTO toDTO(Profile profile) {
        ProfileDTO dto = new ProfileDTO();
        dto.setSummary(profile.getSummary());
        dto.setVisibility(profile.getVisibility());
        dto.setFileUrl(profile.getFileUrl());
        return dto;
    }
}