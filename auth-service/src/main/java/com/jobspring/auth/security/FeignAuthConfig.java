package com.jobspring.auth.security;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignAuthConfig {

    @Bean
    public JwtService jwtService(@Value("${jwt.secret:${JWT_SECRET:dev-default}}") String secret) {
        return new JwtService(secret);
    }

    @Bean
    public RequestInterceptor authHeader(JwtService jwtService,
                                         @Value("${spring.application.name}") String appName) {
        return requestTemplate -> {
            String token = jwtService.issueInternalToken(appName, 10);
            requestTemplate.header("Authorization", "Bearer " + token);
        };
    }
}