package com.jobspring.company.repository;

import com.jobspring.company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    // ✅ 自动生成 SQL：SELECT * FROM companies WHERE name LIKE %keyword%
    List<Company> findByNameContainingIgnoreCase(String keyword);

}
