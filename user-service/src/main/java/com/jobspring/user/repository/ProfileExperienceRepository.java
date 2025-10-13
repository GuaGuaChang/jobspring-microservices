package com.jobspring.user.repository;

import com.jobspring.user.entity.ProfileExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProfileExperienceRepository extends JpaRepository<ProfileExperience, Long> {
    List<ProfileExperience> findByProfileId(Long profileId);
    void deleteByProfileId(Long profileId);
}
