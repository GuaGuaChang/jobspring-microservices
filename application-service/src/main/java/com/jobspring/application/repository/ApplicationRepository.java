package com.jobspring.application.repository;

import com.jobspring.application.entity.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    @Query("select a from Application a where a.userId = :userId order by a.appliedAt desc")
    Page<Application> findMyApplications(Long userId, Pageable pageable);

    @Query("select a from Application a where a.userId = :userId and a.status = :status order by a.appliedAt desc")
    Page<Application> findMyApplicationsByStatus(Long userId, Integer status, Pageable pageable);


    @Query("""
              select a from Application a
              where a.companyId = :companyId
                and (:jobId is null or a.jobId = :jobId)
                and (:status is null or a.status = :status)
              order by a.appliedAt desc
            """)
    Page<Application> searchByCompany(
            @Param("companyId") Long companyId,
            @Param("jobId") Long jobId,
            @Param("status") Integer status,
            Pageable pageable);

    @Query("""
              select a from Application a
              where a.id = :id
            """)
    Optional<Application> findByIdWithJobAndCompany(@Param("id") Long id);

    boolean existsByJobIdAndUserId(Long jobId, Long userId);
}
