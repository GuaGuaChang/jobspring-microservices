package com.jobspring.company.api;

import com.jobspring.company.dto.*;
import com.jobspring.company.entity.Company;
import com.jobspring.company.repository.CompanyRepository;
import com.jobspring.company.service.CompanyService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyRepository companyRepository;

    private final CompanyService companyService;

    /*    private final CompanyMemberService companyMemberService;*/

    // 提供给 job-service 调用的接口
    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponse> getCompanyById(@PathVariable Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found with id: " + id));

        CompanyResponse dto = new CompanyResponse();
        dto.setId(company.getId());
        dto.setName(company.getName());
        dto.setWebsite(company.getWebsite());
        dto.setLogoUrl(company.getLogoUrl());
        dto.setDescription(company.getDescription());

        return ResponseEntity.ok(dto);
    }

    // 提供给 job-service 调用的接口
    @GetMapping("/search")
    public List<Long> findCompanyIdsByName(@RequestParam String keyword) {
        return companyRepository.findByNameContainingIgnoreCase(keyword)
                .stream()
                .map(Company::getId)
                .toList();
    }

    // 提供给 job-service 调用的接口
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

    // 提供给 job-service 调用的接口
    @PostMapping("/batch")
    public Map<Long, CompanyDTO> batch(@RequestBody List<Long> ids) {
        return companyRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Company::getId,
                        c -> new CompanyDTO(c.getId(), c.getName())));
    }

    // 提供给 job-service 调用的接口
    @GetMapping("/hr/{userId}/company-id")
    public Map<String, Long> getCompanyIdByHr(@PathVariable Long userId) {
        Long companyId = companyService.getCompanyIdForHr(userId);
        return Map.of("companyId", companyId);
    }

    @GetMapping("/{companyId}/name")
    public ResponseEntity<String> getCompanyName(@PathVariable Long companyId) {
        return companyRepository.findById(companyId)
                .map(c -> ResponseEntity.ok(c.getName()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{companyId}/jobs")
    public ResponseEntity<PageResponse<JobResponse>> listCompanyJobs(
            @PathVariable Long companyId,
            @RequestParam(required = false) Integer status,
            Pageable pageable
    ) {
        PageResponse<JobResponse> jobs = companyService.listCompanyJobs(
                companyId, status,
                pageable.getPageNumber(), pageable.getPageSize()
        );
        return ResponseEntity.ok(jobs);
    }

}
