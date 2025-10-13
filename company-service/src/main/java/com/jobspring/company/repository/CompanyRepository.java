package com.jobspring.company.repository;

import com.jobspring.company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    boolean existsByName(String name);

    // ✅ 自动生成 SQL：SELECT * FROM companies WHERE name LIKE %keyword%
    List<Company> findByNameContainingIgnoreCase(String keyword);

}
