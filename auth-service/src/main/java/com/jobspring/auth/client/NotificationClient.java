package com.jobspring.auth.client;

import com.jobspring.auth.dto.SendCodeRequestDTO;
import com.jobspring.auth.security.FeignAuthConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "notification-service",
        url = "${NOTIFICATION_BASE_URL:}",
        configuration = FeignAuthConfig.class)
public interface NotificationClient {
    @PostMapping("/send-code")
    void sendVerificationCode(@RequestBody SendCodeRequestDTO request);
}
