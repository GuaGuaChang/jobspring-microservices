package com.jobspring.user.service;

import com.alibaba.nacos.shaded.javax.annotation.Nullable;
import com.jobspring.user.client.AuthUserClient;
import com.jobspring.user.dto.PromoteToHrRequest;
import com.jobspring.user.dto.UserBrief;
import com.jobspring.user.dto.UserView;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final AuthUserClient authUserClient;

    public void makeHr(Long userId, PromoteToHrRequest req) {
        authUserClient.promoteToHr(userId, req);
    }

    public List<UserBrief> batchBrief(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return List.of();

        List<Long> distinct = userIds.stream().distinct().toList();
        List<AuthUserClient.AccountBrief> briefs = authUserClient.batchAccountBriefs(distinct);

        Map<Long, String> nameById = briefs.stream()
                .collect(Collectors.toMap(AuthUserClient.AccountBrief::id, AuthUserClient.AccountBrief::fullName));

        return userIds.stream()
                .map(id -> new UserBrief(id, nameById.getOrDefault(id, "(unknown)")))
                .toList();
    }
}

