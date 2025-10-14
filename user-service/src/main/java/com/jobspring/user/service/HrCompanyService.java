package com.jobspring.user.service;

import com.jobspring.user.client.AuthUserClient;
import com.jobspring.user.client.CompanyClient;
import com.jobspring.user.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HrCompanyService {

    private final AuthUserClient authUserClient;
    private final CompanyClient companyClient;

    public String getMyCompanyName(Long userId) {
        UserDTO user = authUserClient.getAccountById(userId);

        if (user == null || user.getCompanyId() == null) {
            throw new AccessDeniedException("User not bound to any company");
        }

        return companyClient.getCompanyNameById(user.getCompanyId());
    }
}
