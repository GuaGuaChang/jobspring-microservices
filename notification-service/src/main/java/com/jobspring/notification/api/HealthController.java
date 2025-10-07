package com.jobspring.notification.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {
    @GetMapping("/ping")
    public Map<String, String> ping() {
        return Map.of("service", "notification-service", "status", "ok");
    }
}