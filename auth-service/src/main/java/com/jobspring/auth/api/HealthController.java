package com.jobspring.auth.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @Value("${test.message:default}")
    private String message;

    @GetMapping("/auth/ping")
    public String ping() {
        return "auth-service-ok";
    }

    @GetMapping("/auth/config")
    public String config() {
        return message;
    }
}