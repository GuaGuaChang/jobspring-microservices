package com.jobspring.application.repository;

import com.jobspring.application.entity.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    @EntityGraph(attributePaths = {"job", "job.company"})
    @Query("select a from Application a where a.userId = :userId order by a.appliedAt desc")
    Page<Application> findMyApplications(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"job", "job.company"})
    @Query("select a from Application a where a.userId = :userId and a.status = :status order by a.appliedAt desc")
    Page<Application> findMyApplicationsByStatus(Long userId, Integer status, Pageable pageable);
}
