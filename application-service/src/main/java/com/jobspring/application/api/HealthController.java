package com.jobspring.application.api;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class HealthController {
    @GetMapping("/ping")
    public Map<String, String> ping() {
        return Map.of("service", "application-service", "status", "ok");
    }
}