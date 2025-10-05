package com.jobspring.job.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

// value = the name of service in nacos
@FeignClient(name = "auth-service")
public interface AuthClient {

    @GetMapping("/ping")
    String pingFromAuth();
}