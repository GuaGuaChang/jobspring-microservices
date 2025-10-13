package com.jobspring.auth.service;

import com.jobspring.auth.account.Account;
import com.jobspring.auth.account.AccountRepo;
import com.jobspring.auth.client.CompanyClient;
import com.jobspring.auth.dto.PromoteToHrRequest;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int ROLE_CANDIDATE = 0;
    private static final int ROLE_HR = 1;
    private static final int ROLE_ADMIN = 2;

    private final AccountRepo accountRepository;
    private final CompanyClient companyClient;

    public void makeHr(Long userId, PromoteToHrRequest req) {
        Long companyId = null;


        if (req != null && req.getCompanyId() != null) {
            var view = companyClient.getById(req.getCompanyId());
            companyId = view.getId();
        }

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
}
