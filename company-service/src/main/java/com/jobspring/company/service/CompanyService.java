package com.jobspring.company.service;

import com.jobspring.company.client.JobClient;
import com.jobspring.company.dto.CompanyDTO;
import com.jobspring.company.dto.JobResponse;
import com.jobspring.company.dto.PageResponse;
import com.jobspring.company.entity.Company;
import com.jobspring.company.exception.BizException;
import com.jobspring.company.repository.CompanyMemberRepository;
import com.jobspring.company.repository.CompanyRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import com.jobspring.company.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMemberRepository companyMemberRepository;

    private final JobClient jobClient;

    public CompanyDTO getCompanyById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found with id: " + id));

        return convertToCompanyDTO(company);
    }

    public Page<CompanyDTO> getAllCompanies(Pageable pageable) {
        Page<Company> companies = companyRepository.findAll(pageable);
        return companies.map(this::convertToCompanyDTO);
    }


    private CompanyDTO convertToCompanyDTO(Company company) {
        CompanyDTO dto = new CompanyDTO();
        dto.setId(company.getId());
        dto.setName(company.getName());
        dto.setWebsite(company.getWebsite());
        dto.setSize(company.getSize());
        dto.setLogoUrl(company.getLogoUrl());
        dto.setDescription(company.getDescription());
        dto.setCreatedBy(company.getCreatedBy());
        return dto;
    }

    public CompanyDTO createCompany(CompanyDTO dto) {

        if (companyRepository.existsByName(dto.getName())) {
            throw new BizException(ErrorCode.CONFLICT, "already exists");
        }

        Company company = new Company();
        company.setName(dto.getName());
        company.setWebsite(dto.getWebsite());
        company.setSize(dto.getSize());
        company.setLogoUrl(dto.getLogoUrl());
        company.setDescription(dto.getDescription());
        company.setCreatedBy(dto.getCreatedBy());

        Company saved = companyRepository.save(company);

        CompanyDTO result = new CompanyDTO();
        result.setId(saved.getId());
        result.setName(saved.getName());
        result.setWebsite(saved.getWebsite());
        result.setSize(saved.getSize());
        result.setLogoUrl(saved.getLogoUrl());
        result.setDescription(saved.getDescription());
        result.setCreatedBy(saved.getCreatedBy());

        return result;
    }

    public Long findCompanyIdByHr(Long hrUserId) {
        return companyMemberRepository.findCompanyIdByHrUserId(hrUserId)
                .orElseThrow(() -> new EntityNotFoundException("HR not bound to any company"));
    }

    public void assertHrInCompany(Long hrUserId, Long companyId) {
        if (!companyRepository.existsById(companyId)) {
            throw new EntityNotFoundException("Company not found");
        }
        boolean ok = companyMemberRepository.existsByUserIdAndCompanyIdAndRole(hrUserId, companyId, "HR");
        if (!ok) {
            throw new AccessDeniedException("HR not in this company");
        }
    }


    public PageResponse<JobResponse> listCompanyJobs(Long companyId, Integer status, int page, int size) {
        return jobClient.getCompanyJobs(companyId, status, page, size);
    }

}

