package com.jobspring.company.service;

import com.jobspring.company.dto.CompanyDTO;
import com.jobspring.company.entity.Company;
import com.jobspring.company.exception.BizException;
import com.jobspring.company.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import com.jobspring.company.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

//    @Autowired
//    private JobRepository jobRepository;


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
}

