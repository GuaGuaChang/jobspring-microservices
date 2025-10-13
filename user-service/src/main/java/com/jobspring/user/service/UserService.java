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
        authUserClient.promoteToHr(userId, req);
    }
}

