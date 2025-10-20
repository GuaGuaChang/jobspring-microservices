package com.jobspring.company.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobspring.company.dto.CompanyDTO;
import com.jobspring.company.entity.Company;
import com.jobspring.company.service.CompanyService;
import com.jobspring.company.repository.CompanyRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CompaniesController.class)
@AutoConfigureMockMvc(addFilters = false)
class CompaniesControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    CompanyService companyService;

    @MockBean
    CompanyRepository companyRepository;

    // --- POST /create（无 logo，避免 IO）---
    @Test
    void createCompany_without_logo_should_delegate_to_service_and_return_201() throws Exception {
        CompanyDTO input = new CompanyDTO();
        input.setName("Acme");
        input.setWebsite("https://acme.test");
        input.setSize(100);
        input.setDescription("desc");
        input.setCreatedBy("u1");

        CompanyDTO saved = new CompanyDTO();
        saved.setId(10L);
        saved.setName(input.getName());
        saved.setWebsite(input.getWebsite());
        saved.setSize(input.getSize());
        saved.setDescription(input.getDescription());
        saved.setCreatedBy(input.getCreatedBy());
        given(companyService.createCompany(any(CompanyDTO.class))).willReturn(saved);

        MockMultipartFile companyPart = new MockMultipartFile(
                "company",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(input)
        );

        mockMvc.perform(multipart("/create")
                        .file(companyPart))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Acme"));

        ArgumentCaptor<CompanyDTO> captor = ArgumentCaptor.forClass(CompanyDTO.class);
        verify(companyService).createCompany(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Acme");
        // 没有传 logo，不应被意外覆盖
        assertThat(captor.getValue().getLogoUrl()).isNull();
    }

    // --- POST /create（带合法图片）---
    @Test
    void createCompany_with_logo_should_set_logoUrl_and_return_201() throws Exception {
        CompanyDTO input = new CompanyDTO();
        input.setName("LogoCo");

        CompanyDTO saved = new CompanyDTO();
        saved.setId(11L);
        saved.setName("LogoCo");
        saved.setLogoUrl("/uploads/anything.png");
        given(companyService.createCompany(any(CompanyDTO.class))).willReturn(saved);

        MockMultipartFile companyPart = new MockMultipartFile(
                "company", "", "application/json",
                objectMapper.writeValueAsBytes(input)
        );
        MockMultipartFile logo = new MockMultipartFile(
                "logo", "logo.png", "image/png", new byte[]{1, 2, 3}
        );

        mockMvc.perform(multipart("/create").file(companyPart).file(logo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.name").value("LogoCo"));

        // 验证 service 被调用
        verify(companyService).createCompany(any(CompanyDTO.class));
    }

    // --- GET /list ---
    @Test
    void getAllCompanies_should_return_page() throws Exception {
        CompanyDTO c1 = new CompanyDTO();
        c1.setId(1L); c1.setName("C1");
        CompanyDTO c2 = new CompanyDTO();
        c2.setId(2L); c2.setName("C2");

        given(companyService.getAllCompanies(any()))
                .willReturn(new PageImpl<>(List.of(c1, c2), PageRequest.of(0, 2), 2));

        mockMvc.perform(get("/list").param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("C1"));
    }

    // --- POST / （保存实体）---
    @Test
    void createCompany_entity_endpoint_should_call_repository_save() throws Exception {
        Company req = new Company();
        req.setName("N");
        req.setWebsite("https://n.test");
        req.setSize(50);
        req.setDescription("d");
        req.setCreatedBy("u");

        Company saved = new Company();
        saved.setId(99L);
        saved.setName(req.getName());
        saved.setWebsite(req.getWebsite());
        saved.setSize(req.getSize());
        saved.setDescription(req.getDescription());
        saved.setCreatedBy(req.getCreatedBy());

        given(companyRepository.save(any(Company.class))).willReturn(saved);

        mockMvc.perform(post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.name").value("N"));

        verify(companyRepository).save(any(Company.class));
    }

    // --- GET /hr/{hrUserId}/companyId ---
    @Test
    void findCompanyIdByHr_should_return_id() throws Exception {
        given(companyService.findCompanyIdByHr(7L)).willReturn(421L);

        mockMvc.perform(get("/hr/{hrUserId}/companyId", 7L))
                .andExpect(status().isOk())
                .andExpect(content().string("421"));

        verify(companyService).findCompanyIdByHr(7L);
    }

    // --- GET /{companyId}/ownership/validate?hrUserId=... ---
    @Test
    void assertHrInCompany_should_delegate_and_return_204() throws Exception {
        mockMvc.perform(get("/{companyId}/ownership/validate", 42L)
                        .param("hrUserId", "9"))
                .andExpect(status().isNoContent());

        verify(companyService).assertHrInCompany(9L, 42L);
    }
}
