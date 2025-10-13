package com.jobspring.user.repository;

import com.jobspring.user.entity.JobFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobFavoriteRepository extends JpaRepository<JobFavorite, Long> {
    boolean existsByUserIdAndJobId(Long userId, Long jobId);

    void deleteByUserIdAndJobId(Long userId, Long jobId);

    Page<JobFavorite> findByUserId(Long userId, Pageable pageable);

}
