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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CompaniesController {

    private final CompanyRepository companyRepository;


    private final CompanyService companyService;


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CompanyDTO> createCompany(
            @RequestPart("company") CompanyDTO companyDTO,
            @RequestPart(value = "logo", required = false) MultipartFile logoFile) throws IOException {

        if (logoFile != null && !logoFile.isEmpty()) {
            // 1) 基础目录：绝对路径 + 规范化
            Path baseDir = Paths.get("uploads").toAbsolutePath().normalize();
            Files.createDirectories(baseDir);

            long maxBytes = 10L * 1024 * 1024; // 5MB
            if (logoFile.getSize() > maxBytes) {
                throw new IllegalArgumentException("Logo file too large");
            }
            String original = logoFile.getOriginalFilename();
            String ext = "";
            if (original != null) {
                int dot = original.lastIndexOf('.');
                if (dot >= 0 && dot < original.length() - 1) {
                    ext = original.substring(dot + 1).toLowerCase();
                }
            }
            Set<String> allowed = Set.of("png", "jpg", "jpeg", "gif", "webp");
            if (!allowed.contains(ext)) {
                throw new IllegalArgumentException("Unsupported logo file type");
            }

            String filename = System.currentTimeMillis() + "_" + UUID.randomUUID() + "." + ext;

            Path target = baseDir.resolve(filename).normalize();
            if (!target.startsWith(baseDir)) {
                throw new SecurityException("Invalid path");
            }

            try (InputStream in = logoFile.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            companyDTO.setLogoUrl("/uploads/" + filename);
        }

        CompanyDTO savedCompany = companyService.createCompany(companyDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedCompany);
    }

    @GetMapping("/list")
    public ResponseEntity<Page<CompanyDTO>> getAllCompanies(Pageable pageable) {
        Page<CompanyDTO> companies = companyService.getAllCompanies(pageable);
        return ResponseEntity.ok(companies);
    }


    @PostMapping
    public Company createCompany(@RequestBody Company req) {
        return companyRepository.save(req);
    }

    @GetMapping("/hr/{hrUserId}/company-id")
    public Long findCompanyIdByHr(@PathVariable Long hrUserId) {
        return companyService.findCompanyIdByHr(hrUserId);
    }


    @GetMapping("/{companyId}/ownership/validate")
    public ResponseEntity<Void> assertHrInCompany(
            @RequestParam Long hrUserId,
            @PathVariable Long companyId) {
        companyService.assertHrInCompany(hrUserId, companyId);
        return ResponseEntity.noContent().build();
    }

}
