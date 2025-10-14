package com.jobspring.user.repository;

import com.jobspring.user.entity.ProfileEducation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProfileEducationRepository extends JpaRepository<ProfileEducation, Long> {
    List<ProfileEducation> findByProfileId(Long profileId);
    void deleteByProfileId(Long profileId);
}
