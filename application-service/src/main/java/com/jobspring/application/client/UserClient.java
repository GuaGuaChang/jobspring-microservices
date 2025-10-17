package com.jobspring.application.client;


import com.jobspring.application.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "user-service", url = "${USER_BASE_URL:}")
public interface UserClient {
    @PostMapping("/briefs:batch")
    List<UserBrief> batchBrief(@RequestBody List<Long> userIds);

    record UserBrief(Long id, String fullName) {
    }

    @GetMapping("/users/{id}")
    UserDTO getUserById(@PathVariable("id") Long id);
}
