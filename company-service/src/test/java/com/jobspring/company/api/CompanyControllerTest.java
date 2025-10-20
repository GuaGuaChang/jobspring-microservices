package com.jobspring.company.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobspring.company.dto.CompanyDTO;
import com.jobspring.company.dto.CompanyFavouriteResponse;
import com.jobspring.company.dto.JobResponse;
import com.jobspring.company.dto.PageResponse;
import com.jobspring.company.entity.Company;
import com.jobspring.company.repository.CompanyRepository;
import com.jobspring.company.service.CompanyService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 仅加载 CompanyController 的 Web 层，Repository/Service 用 @MockBean 注入
 */
@WebMvcTest(CompanyController.class)
@AutoConfigureMockMvc(addFilters = false) // 若工程启用了 Security，关闭过滤器避免 401/403
class CompanyControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    CompanyRepository companyRepository;

    @MockBean
    CompanyService companyService;

    private Company company(Long id, String name) {
        Company c = new Company();
        c.setId(id);
        c.setName(name);
        c.setWebsite("https://example.com/" + id);
        c.setLogoUrl("https://logo/" + id + ".png");
        c.setDescription("desc-" + id);
        return c;
    }

    @Test
    void getCompanyById_should_return_dto() throws Exception {
        Company c = company(10L, "Acme");
        given(companyRepository.findById(10L)).willReturn(Optional.of(c));

        mockMvc.perform(get("/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Acme"))
                .andExpect(jsonPath("$.website").value("https://example.com/10"))
                .andExpect(jsonPath("$.logoUrl").value("https://logo/10.png"))
                .andExpect(jsonPath("$.description").value("desc-10"));
    }

    @Test
    void search_should_return_ids() throws Exception {
        given(companyRepository.findByNameContainingIgnoreCase("ac"))
                .willReturn(List.of(company(1L, "Acme"), company(2L, "Acorn")));

        mockMvc.perform(get("/search").param("keyword", "ac"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0]").value(1))
                .andExpect(jsonPath("$[1]").value(2));
    }

    @Test
    void jobFavourite_should_return_minimal_fields() throws Exception {
        Company c = company(5L, "Beta");
        given(companyRepository.findById(5L)).willReturn(Optional.of(c));

        mockMvc.perform(get("/job-favourite/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Beta"))
                .andExpect(jsonPath("$.website").value("https://example.com/5"));
    }

    @Test
    void batch_should_return_map_of_id_to_dto() throws Exception {
        List<Long> ids = List.of(3L, 4L);
        List<Company> companies = List.of(
                company(3L, "C3"),
                company(4L, "C4")
        );
        given(companyRepository.findAllById(ids)).willReturn(companies);

        mockMvc.perform(post("/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk())
                // 取 map 的 key=3、4
                .andExpect(jsonPath("$['3'].id").value(3))
                .andExpect(jsonPath("$['3'].name").value("C3"))
                .andExpect(jsonPath("$['4'].id").value(4))
                .andExpect(jsonPath("$['4'].name").value("C4"));
    }


    @Test
    void getCompanyName_should_return_name_or_404() throws Exception {
        given(companyRepository.findById(9L)).willReturn(Optional.of(company(9L, "Nine")));
        mockMvc.perform(get("/9/name"))
                .andExpect(status().isOk())
                .andExpect(content().string("Nine"));

        given(companyRepository.findById(99L)).willReturn(Optional.empty());
        mockMvc.perform(get("/99/name"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listCompanyJobs_should_delegate_to_service() throws Exception {
        PageResponse<JobResponse> page = new PageResponse<>();
        page.setNumber(0);
        page.setSize(2);
        page.setTotalElements(2L);
        page.setTotalPages(1);
        page.setLast(true);

        JobResponse r1 = new JobResponse();
        r1.setId(1L);
        r1.setCompanyId(100L);
        r1.setTitle("J1");
        r1.setLocation("NY");
        r1.setEmploymentType(1);
        r1.setSalaryMin(new BigDecimal("1"));
        r1.setSalaryMax(new BigDecimal("2"));
        r1.setDescription("d1");
        r1.setStatus(0);
        r1.setPostedAt(LocalDateTime.now());

        JobResponse r2 = new JobResponse();
        r2.setId(2L);
        r2.setCompanyId(100L);
        r2.setTitle("J2");
        r2.setLocation("SF");
        r2.setEmploymentType(2);
        r2.setSalaryMin(new BigDecimal("3"));
        r2.setSalaryMax(new BigDecimal("4"));
        r2.setDescription("d2");
        r2.setStatus(1);
        r2.setPostedAt(LocalDateTime.now());

        page.setContent(List.of(r1, r2));

        given(companyService.listCompanyJobs(eq(100L), eq(0), eq(0), eq(2)))
                .willReturn(page);

        mockMvc.perform(get("/100/jobs")
                        .param("status", "0")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].title").value("J1"))
                .andExpect(jsonPath("$.content[1].title").value("J2"));
    }


    @Test
    void getCompanyIdByHr_should_delegate_to_service() throws Exception {
        given(companyService.findCompanyIdByHr(7L)).willReturn(42L);

        mockMvc.perform(get("/hr/7/company-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyId").value(42));
    }
}
