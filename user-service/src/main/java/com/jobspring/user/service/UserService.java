package com.jobspring.user.service;

import com.alibaba.nacos.shaded.javax.annotation.Nullable;
import com.jobspring.user.client.AuthUserClient;
import com.jobspring.user.dto.PromoteToHrRequest;
import com.jobspring.user.dto.UserView;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final AuthUserClient authUserClient;

    public void makeHr(Long userId, PromoteToHrRequest req) {
//        UserView u = authUserClient.getUser(userId);
//        if (u == null) throw new EntityNotFoundException("User not found");
//
//        if (Boolean.FALSE.equals(u.getActive())) {
//            throw new IllegalStateException("User is inactive");
//        }
//        if ("ADMIN".equals(u.getRole())) {
//            throw new IllegalArgumentException("Cannot change role of ADMIN");
//        }
//        if (!"HR".equals(u.getRole()) && !"CANDIDATE".equals(u.getRole())) {
//            throw new IllegalArgumentException("Only candidate can be promoted to HR");
//        }

        authUserClient.promoteToHr(userId, req);
    }
}

