package com.jobspring.company.repository;

import com.jobspring.company.entity.CompanyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyMemberRepository extends JpaRepository<CompanyMember, Long> {

    @Query("""
         select cm.company.id
         from CompanyMember cm
         where cm.userId = :userId and cm.role = 'HR'
         """)
    Optional<Long> findCompanyIdByHrUserId(@Param("userId") Long userId);
}
