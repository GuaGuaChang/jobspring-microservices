package com.jobspring.job.api;

import com.jobspring.job.client.AuthClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class JobController {
    private final AuthClient authClient;

    @GetMapping("/job/test")
    public String test() {
        return "job-service-ok";
    }

    @GetMapping("/job/ping-auth")
    public String callAuth() {
        String result = authClient.pingFromAuth();
        return "job-service → " + result;
    }

}