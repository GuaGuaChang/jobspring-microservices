package com.jobspring.company.api;

import com.jobspring.company.dto.CompanyFavouriteResponse;
import com.jobspring.company.dto.CompanyResponse;
import com.jobspring.company.entity.Company;
import com.jobspring.company.repository.CompanyRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyRepository companyRepository;

    // ✅ 提供给 job-service 调用的接口
    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponse> getCompanyById(@PathVariable Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found with id: " + id));

        CompanyResponse dto = new CompanyResponse();
        dto.setId(company.getId());
        dto.setName(company.getName());
        dto.setLogoUrl(company.getLogoUrl());
        dto.setDescription(company.getDescription());

        return ResponseEntity.ok(dto);
    }



    // ✅ 提供给 job-service 调用的接口
    @GetMapping("/search")
    public List<Long> findCompanyIdsByName(@RequestParam String keyword) {
        return companyRepository.findByNameContainingIgnoreCase(keyword)
                .stream()
                .map(Company::getId)
                .toList();
    }

    @GetMapping("/job-favourite/{id}")
    public ResponseEntity<CompanyFavouriteResponse> get(@PathVariable Long id) {
        Company c = companyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Company not found"));
        CompanyFavouriteResponse r = new CompanyFavouriteResponse();
        r.setId(c.getId());
        r.setName(c.getName());
        r.setWebsite(c.getWebsite());
        return ResponseEntity.ok(r);
    }
}
