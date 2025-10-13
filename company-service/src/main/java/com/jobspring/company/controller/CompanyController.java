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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

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
            // 1) 基础目录：绝对路径 + 规范化
            Path baseDir = Paths.get("uploads").toAbsolutePath().normalize();
            Files.createDirectories(baseDir);

            // 2) 控制大小（可根据需要调整）
            long maxBytes = 10L * 1024 * 1024; // 5MB
            if (logoFile.getSize() > maxBytes) {
                throw new IllegalArgumentException("Logo file too large");
            }
            // 3) 只取扩展名并做白名单（不要信任原始文件名）
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

            // 4) 生成服务器端文件名（避免用户控制）
            String filename = System.currentTimeMillis() + "_" + UUID.randomUUID() + "." + ext;

            // 5) 规范化 + 越界检查
            Path target = baseDir.resolve(filename).normalize();
            if (!target.startsWith(baseDir)) {
                // 即使我们没有使用原始文件名，也保留这一防线
                throw new SecurityException("Invalid path");
            }

            // 6) 落盘（try-with-resources；可选去掉 REPLACE_EXISTING）
            try (InputStream in = logoFile.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

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
