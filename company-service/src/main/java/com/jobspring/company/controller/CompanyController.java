package com.jobspring.company.controller;


import com.jobspring.company.dto.CompanyDTO;
import com.jobspring.company.entity.Company;
import com.jobspring.company.repository.CompanyRepository;
import com.jobspring.company.service.CompanyService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@RestController
//@RequiredArgsConstructor
public class CompanyController {

    private final CompanyRepository companyRepository;


    private final CompanyService companyService;

    public CompanyController(CompanyRepository companyRepository, CompanyService companyService) {
        this.companyRepository = companyRepository;
        this.companyService = companyService;
    }


    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CompanyDTO> createCompany(
            @RequestPart("company") CompanyDTO companyDTO,
            @RequestPart(value = "logo", required = false) MultipartFile logoFile) throws IOException {

        if (logoFile != null && !logoFile.isEmpty()) {
            String filename = System.currentTimeMillis() + "_" + logoFile.getOriginalFilename();
            Path uploadPath = Paths.get("uploads"); // 相对路径
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(filename);
            Files.copy(logoFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            companyDTO.setLogoUrl("/uploads/" + filename); // 前端访问 URL
        }

        CompanyDTO savedCompany = companyService.createCompany(companyDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedCompany);
    }

    @GetMapping("/company/list")
    public ResponseEntity<Page<CompanyDTO>> getAllCompanies(Pageable pageable) {
        Page<CompanyDTO> companies = companyService.getAllCompanies(pageable);
        return ResponseEntity.ok(companies);
    }
    // 创建公司
    @PostMapping
    public Company createCompany(@RequestBody Company req) {
        return companyRepository.save(req);
    }


}
