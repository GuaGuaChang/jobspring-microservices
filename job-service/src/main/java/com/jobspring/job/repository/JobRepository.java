package com.jobspring.job.repository;

import com.jobspring.job.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
    Page<Job> findByStatus(@Param("status") Integer status, Pageable pageable);

    @Query("""
                SELECT j FROM Job j
                WHERE j.status = 0
                  AND (
                       LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(j.location) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR j.companyId IN :companyIds
                  )
            """)
    Page<Job> searchJobs(@Param("keyword") String keyword,
                         @Param("companyIds") List<Long> companyIds,
                         Pageable pageable);

    Optional<Job> findByIdAndCompanyId(Long jobId, Long companyId);

/*    Page<Job> findByCompanyId(Long companyId, Pageable pageable);*/

    Page<Job> findByCompanyIdAndStatus(Long companyId, Integer status, Pageable pageable);

    List<Job> findByCompanyId(Long companyId);
}