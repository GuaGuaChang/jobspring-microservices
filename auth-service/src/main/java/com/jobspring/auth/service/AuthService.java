package com.jobspring.auth.service;

import com.jobspring.auth.account.Account;
import com.jobspring.auth.account.AccountRepo;
import com.jobspring.auth.client.CompanyClient;
import com.jobspring.auth.dto.AccountBrief;
import com.jobspring.auth.dto.PromoteToHrRequest;
import com.jobspring.auth.dto.UserDTO;
import com.jobspring.auth.repository.spec.UserSpecs;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int ROLE_CANDIDATE = 0;
    private static final int ROLE_HR = 1;
    private static final int ROLE_ADMIN = 2;

    private final AccountRepo accountRepository;


    public void makeHr(Long userId, PromoteToHrRequest req) {
        Long companyId = null;
        companyId = req.getCompanyId();
        doPromoteHr(userId, companyId, req == null ? null : req.getOverwriteCompany());
    }

    @Transactional
    protected void doPromoteHr(Long userId, Long companyId, Boolean overwriteCompany) {
        Account u = accountRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (Boolean.FALSE.equals(u.getActive())) {
            throw new IllegalStateException("User is inactive");
        }
        if (u.getRole() == ROLE_ADMIN) {
            throw new IllegalArgumentException("Cannot change role of ADMIN");
        }
        if (u.getRole() != ROLE_HR && u.getRole() != ROLE_CANDIDATE) {
            throw new IllegalArgumentException("Only candidate can be promoted to HR");
        }


        u.setRole(ROLE_HR);

        if (companyId != null) {
            boolean overwrite = overwriteCompany == null || overwriteCompany;
            if (u.getCompanyId() == null || overwrite) {
                u.setCompanyId(companyId);

            }
        }

        accountRepository.save(u);
    }

    public Page<UserDTO> searchUsers(String q, Pageable pageable) {
        if (q == null || q.isBlank()) {
            return accountRepository.findAll(pageable).map(this::toDTO);
        }
        String norm = q.trim();

        Page<Account> page = accountRepository.findAll(UserSpecs.fuzzySearch(norm), pageable);
        return page.map(this::toDTO);
    }


    private UserDTO toDTO(Account user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setFullName(user.getFullName());
        dto.setRole(user.getRole());
        dto.setIsActive(user.getActive());
        return dto;
    }

    public List<AccountBrief> batchBrief(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return accountRepository.findAllById(ids).stream()
                .map(a -> new AccountBrief(a.getId(), a.getFullName()))
                .toList();
    }


    public UserDTO getUserById(Long id) {
        Account a = accountRepository.getUserById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        UserDTO dto = new UserDTO();
        dto.setId(a.getId());
        dto.setFullName(a.getFullName());
        dto.setEmail(a.getEmail());
        dto.setRole(a.getRole());
        return dto;
    }
}
