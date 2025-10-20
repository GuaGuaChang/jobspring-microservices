package com.jobspring.company.service;

import com.jobspring.company.client.JobClient;
import com.jobspring.company.dto.CompanyDTO;
import com.jobspring.company.dto.JobResponse;
import com.jobspring.company.dto.PageResponse;
import com.jobspring.company.entity.Company;
import com.jobspring.company.exception.BizException;
import com.jobspring.company.exception.ErrorCode;
import com.jobspring.company.repository.CompanyMemberRepository;
import com.jobspring.company.repository.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    CompanyRepository companyRepository;

    @Mock
    CompanyMemberRepository companyMemberRepository;

    @Mock
    JobClient jobClient;

    @InjectMocks
    CompanyService companyService;

    // 帮助方法：用正确类型
    private Company company(Long id, String name) {
        Company c = new Company();
        c.setId(id);
        c.setName(name);
        c.setWebsite("https://example.com/" + id);
        c.setSize(100);                      // Integer，而不是 "100-499"
        c.setLogoUrl("logo-" + id);
        c.setDescription("desc-" + id);
        c.setCreatedBy("123");               // String，而不是 123L
        return c;
    }


    @Test
    void getCompanyById_should_map_all_fields() {
        Company src = company(10L, "Acme");
        given(companyRepository.findById(10L)).willReturn(Optional.of(src));

        CompanyDTO dto = companyService.getCompanyById(10L);

        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getName()).isEqualTo("Acme");
        assertThat(dto.getWebsite()).isEqualTo("https://example.com/10");
        assertThat(dto.getSize()).isEqualTo(100);
        assertThat(dto.getLogoUrl()).isEqualTo("logo-10");
        assertThat(dto.getDescription()).isEqualTo("desc-10");
        assertThat(dto.getCreatedBy()).isEqualTo("123");

    }

    @Test
    void getAllCompanies_should_page_and_map() {
        Pageable pageable = PageRequest.of(0, 2, Sort.by("id").ascending());
        List<Company> content = List.of(company(1L, "A"), company(2L, "B"));
        Page<Company> page = new PageImpl<>(content, pageable, 2);
        given(companyRepository.findAll(pageable)).willReturn(page);

        Page<CompanyDTO> res = companyService.getAllCompanies(pageable);

        assertThat(res.getTotalElements()).isEqualTo(2);
        assertThat(res.getContent()).extracting("name").containsExactly("A", "B");
    }

    @Test
    void createCompany_should_save_and_return_dto() {
        CompanyDTO input = new CompanyDTO();
        input.setName("NewCo");
        input.setWebsite("https://new.co");
        input.setSize(50);                   // Integer
        input.setLogoUrl("logo-new");
        input.setDescription("desc");
        input.setCreatedBy("u1");            // String

        given(companyRepository.existsByName("NewCo")).willReturn(false);

        ArgumentCaptor<Company> cap = ArgumentCaptor.forClass(Company.class);
        given(companyRepository.save(any(Company.class))).willAnswer(inv -> {
            Company arg = inv.getArgument(0);
            arg.setId(88L);
            return arg;
        });

        CompanyDTO out = companyService.createCompany(input);

        verify(companyRepository).save(cap.capture());
        assertThat(cap.getValue().getSize()).isEqualTo(50);
        assertThat(cap.getValue().getCreatedBy()).isEqualTo("u1");

        assertThat(out.getId()).isEqualTo(88L);
        assertThat(out.getSize()).isEqualTo(50);
        assertThat(out.getCreatedBy()).isEqualTo("u1");
    }


    @Test
    void createCompany_when_name_conflict_should_throw() {
        CompanyDTO input = new CompanyDTO();
        input.setName("Dup");

        given(companyRepository.existsByName("Dup")).willReturn(true);

        assertThatThrownBy(() -> companyService.createCompany(input))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("already exists")
                .extracting(e -> ((BizException) e).getErrorCode())
                .isEqualTo(ErrorCode.CONFLICT);
        verify(companyRepository, never()).save(any());
    }

    @Test
    void findCompanyIdByHr_should_return_value_from_repo() {
        given(companyMemberRepository.findCompanyIdByHrUserId(7L))
                .willReturn(Optional.of(100L));

        Long companyId = companyService.findCompanyIdByHr(7L);
        assertThat(companyId).isEqualTo(100L);
    }

    @Test
    void findCompanyIdByHr_when_not_found_should_throw() {
        given(companyMemberRepository.findCompanyIdByHrUserId(7L))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> companyService.findCompanyIdByHr(7L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void assertHrInCompany_should_pass_when_exists_and_binding_ok() {
        given(companyRepository.existsById(42L)).willReturn(true);
        given(companyMemberRepository.existsByUserIdAndCompanyIdAndRole(7L, 42L, "HR"))
                .willReturn(true);

        companyService.assertHrInCompany(7L, 42L); // 不抛异常即通过
    }

    @Test
    void assertHrInCompany_should_throw_when_company_missing() {
        given(companyRepository.existsById(42L)).willReturn(false);

        assertThatThrownBy(() -> companyService.assertHrInCompany(7L, 42L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void assertHrInCompany_should_throw_when_not_bound_as_hr() {
        given(companyRepository.existsById(42L)).willReturn(true);
        given(companyMemberRepository.existsByUserIdAndCompanyIdAndRole(7L, 42L, "HR"))
                .willReturn(false);

        assertThatThrownBy(() -> companyService.assertHrInCompany(7L, 42L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void ListCompanyJobs_should_delegate_to_jobClient() {
        PageResponse<JobResponse> page = new PageResponse<>();
        page.setNumber(0);
        page.setSize(1);
        page.setTotalElements(1L);
        page.setTotalPages(1);
        page.setLast(true);

        JobResponse r = new JobResponse();
        r.setId(1L);
        r.setCompanyId(42L);
        r.setTitle("J1");
        page.setContent(List.of(r));

        given(jobClient.getCompanyJobs(42L, 0, 0, 1)).willReturn(page);

        PageResponse<JobResponse> res = companyService.listCompanyJobs(42L, 0, 0, 1);

        assertThat(res.getSize()).isEqualTo(1);
        assertThat(res.getNumber()).isEqualTo(0);
        assertThat(res.getTotalElements()).isEqualTo(1L);
        verify(jobClient).getCompanyJobs(42L, 0, 0, 1);
    }

}
