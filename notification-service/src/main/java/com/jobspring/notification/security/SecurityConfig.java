package com.jobspring.notification.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {

    @Bean
    public JwtService jwtService(@Value("${jwt.secret:${JWT_SECRET:dev-default}}") String secret) {
        return new JwtService(secret);
    }

    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtFilter(JwtService jwtService) {
        FilterRegistrationBean<JwtAuthFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new JwtAuthFilter(jwtService));
        bean.addUrlPatterns("/*");
        return bean;
    }
}