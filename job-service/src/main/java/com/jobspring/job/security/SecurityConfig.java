package com.jobspring.job.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableMethodSecurity          // 让 @PreAuthorize("hasRole('ADMIN')") 生效
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // 使用 JWT/自定义 Header 的无状态认证
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 不再全局关闭 CSRF：启用 CSRF，并仅对少量端点忽略
                .csrf(csrf -> csrf
                        // 把 CSRF token 放在 Cookie 里（前端需要时可读，withHttpOnlyFalse）
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        // 仅忽略这些路径的 CSRF 校验：运维端点、只读 GET、公开资源等
                        .ignoringRequestMatchers(
                                new AntPathRequestMatcher("/companies/**"),
                                new AntPathRequestMatcher("/actuator/**")
                        )
                )

                .authorizeHttpRequests(reg -> reg
                        .requestMatchers("/actuator/**", "/public/**").permitAll()
                        .requestMatchers("/companies/**").permitAll()
                        .anyRequest().authenticated()
                )

                // 你的 Header 认证过滤器（确保能把角色写成 ROLE_ADMIN / ROLE_XXX）
                .addFilterBefore(new HeaderAuthFilter(), UsernamePasswordAuthenticationFilter.class)

                .build();
    }
}