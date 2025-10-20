package com.jobspring.user.service;

import com.jobspring.user.client.JobClient;
import com.jobspring.user.dto.*;
import com.jobspring.user.entity.Profile;
import com.jobspring.user.entity.ProfileEducation;
import com.jobspring.user.entity.ProfileExperience;
import com.jobspring.user.entity.UserSkill;
import com.jobspring.user.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProfileServiceTest {

    ProfileRepository profileRepository = mock(ProfileRepository.class);
    ProfileEducationRepository educationRepository = mock(ProfileEducationRepository.class);
    ProfileExperienceRepository experienceRepository = mock(ProfileExperienceRepository.class);
    UserSkillRepository userSkillRepository = mock(UserSkillRepository.class);
    JobClient jobClient = mock(JobClient.class);

    ProfileService service;

    @BeforeEach
    void setUp() {
        service = new ProfileService();
        // 反射注入（也可用 @InjectMocks）
        set(service, "profileRepository", profileRepository);
        set(service, "educationRepository", educationRepository);
        set(service, "experienceRepository", experienceRepository);
        set(service, "userSkillRepository", userSkillRepository);
        set(service, "jobClient", jobClient);
    }

    // -------- getCompleteProfile --------

    @Test
    void getCompleteProfile_should_assemble_all_sections() {
        Profile p = new Profile();
        p.setId(11L);
        p.setUserId(123L);
        p.setSummary("sum");
        p.setVisibility(0);
        p.setFileUrl("cv.pdf");

        when(profileRepository.findByUserId(123L)).thenReturn(Optional.of(p));
        when(educationRepository.findByProfileId(11L)).thenReturn(List.of(new ProfileEducation()));
        when(experienceRepository.findByProfileId(11L)).thenReturn(List.of(new ProfileExperience()));
        UserSkill us = new UserSkill();
        us.setUserId(123L);
        us.setSkillId(7L);
        us.setLevel(3);
        us.setYears(BigDecimal.valueOf(2));
        when(userSkillRepository.findByUserId(123L)).thenReturn(List.of(us));

        SkillDTO s = new SkillDTO();
        s.setId(7L); s.setName("Java"); s.setCategory("Backend");
        when(jobClient.getAllSkills()).thenReturn(List.of(s));

        ProfileResponseDTO dto = service.getCompleteProfile(123L);

        assertThat(dto.getProfile().getSummary()).isEqualTo("sum");
        assertThat(dto.getSkills()).hasSize(1);
        assertThat(dto.getSkills().get(0).getSkillName()).isEqualTo("Java");
    }

    @Test
    void getCompleteProfile_when_jobClient_fails_should_fill_unknown() {
        Profile p = new Profile();
        p.setId(11L); p.setUserId(123L);
        when(profileRepository.findByUserId(123L)).thenReturn(Optional.of(p));
        when(educationRepository.findByProfileId(11L)).thenReturn(List.of());
        when(experienceRepository.findByProfileId(11L)).thenReturn(List.of());
        UserSkill us = new UserSkill();
        us.setUserId(123L); us.setSkillId(99L);
        when(userSkillRepository.findByUserId(123L)).thenReturn(List.of(us));
        when(jobClient.getAllSkills()).thenThrow(new RuntimeException("down"));

        ProfileResponseDTO dto = service.getCompleteProfile(123L);
        assertThat(dto.getSkills()).singleElement().extracting("skillName").isEqualTo("(unknown)");
    }

    // -------- createOrUpdateProfile --------

    @Test
    void createOrUpdateProfile_should_save_profile_and_children_and_skills() {
        long userId = 123L;

        // 走「新建」分支
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // save 时给 profile 赋 id，模拟 JPA 回填
        when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> {
            Profile p = inv.getArgument(0);
            p.setId(11L);
            return p;
        });

        // 校验技能用
        SkillDTO s1 = new SkillDTO(); s1.setId(1L);
        SkillDTO s2 = new SkillDTO(); s2.setId(2L);
        when(jobClient.getAllSkills()).thenReturn(List.of(s1, s2));

        // 组装请求（注意：visibility/level 用 Integer）
        ProfileRequestDTO req = new ProfileRequestDTO();
        ProfileDTO pd = new ProfileDTO();
        pd.setSummary("A");
        pd.setVisibility(0);                // 0=PUBLIC
        pd.setFileUrl("cv.pdf");
        req.setProfile(pd);

        EducationDTO ed = new EducationDTO(); ed.setSchool("X");
        req.setEducation(List.of(ed));

        ExperienceDTO ex = new ExperienceDTO(); ex.setCompany("Y");
        req.setExperience(List.of(ex));

        UserSkillDTO usd1 = new UserSkillDTO(); usd1.setSkillId(1L); usd1.setLevel(3); usd1.setYears(2.0);
        UserSkillDTO usd2 = new UserSkillDTO(); usd2.setSkillId(2L); usd2.setLevel(4); usd2.setYears(5.0);
        req.setSkills(List.of(usd1, usd2));

        // 执行
        service.createOrUpdateProfile(userId, req);

        // 验证
        verify(profileRepository, times(1)).save(any(Profile.class)); // 只会保存一次
        verify(educationRepository).deleteByProfileId(11L);
        verify(experienceRepository).deleteByProfileId(11L);
        verify(educationRepository, atLeastOnce()).save(any(ProfileEducation.class));
        verify(experienceRepository, atLeastOnce()).save(any(ProfileExperience.class));

        verify(userSkillRepository).deleteByUserId(userId);
        ArgumentCaptor<List<UserSkill>> cap = ArgumentCaptor.forClass(List.class);
        verify(userSkillRepository).saveAll(cap.capture());
        List<UserSkill> saved = cap.getValue();
        assertThat(saved).hasSize(2);
        assertThat(saved.get(0).getSkillId()).isIn(1L, 2L);
        assertThat(saved.get(0).getLevel()).isIn(3, 4);
    }

    @Test
    void createOrUpdateProfile_when_skillId_invalid_should_400() {
        when(profileRepository.findByUserId(123L)).thenReturn(Optional.of(new Profile(){{
            setId(11L); setUserId(123L);
        }}));
        when(jobClient.getAllSkills()).thenReturn(List.of()); // 无有效 id

        ProfileRequestDTO req = new ProfileRequestDTO();
        ProfileDTO pd = new ProfileDTO(); pd.setSummary("A"); pd.setVisibility(0);
        req.setProfile(pd);

        UserSkillDTO usd = new UserSkillDTO(); usd.setSkillId(999L);
        req.setSkills(List.of(usd));

        assertThatThrownBy(() -> service.createOrUpdateProfile(123L, req))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid skillId");

        verify(userSkillRepository, never()).saveAll(any());
    }

    // ---- 简单的反射 setter，避免引第三方注入工具 ----
    private static void set(Object target, String field, Object value) {
        try {
            var f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
