package com.jobspring.application.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "user-service")
public interface UserClient {
    @PostMapping("/briefs:batch")
    List<UserBrief> batchBrief(@RequestBody List<Long> userIds);

    record UserBrief(Long id, String fullName) {
    }
}
