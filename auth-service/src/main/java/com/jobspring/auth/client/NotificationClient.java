package com.jobspring.auth.client;

import com.jobspring.auth.dto.SendCodeRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service")
public interface NotificationClient {
    @PostMapping("/send-code")
    void sendVerificationCode(@RequestBody SendCodeRequestDTO request);
}
