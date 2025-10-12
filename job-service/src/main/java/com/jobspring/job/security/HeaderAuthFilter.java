package com.jobspring.job.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class HeaderAuthFilter extends OncePerRequestFilter {

    private static String toRoleName(String roleClaim) {
        // 你的 token 里 role 是 0/1/2，可按项目枚举来映射
        return switch (String.valueOf(roleClaim)) {
            case "0", "CANDIDATE" -> "CANDIDATE";
            case "1", "HR"        -> "HR";
            case "2", "ADMIN"     -> "ADMIN";
            default               -> "CANDIDATE";
        };
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws ServletException, IOException {

        String uid  = req.getHeader("X-User-Id");
        String role = req.getHeader("X-User-Role");

        if (uid != null && role != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            var authority = new SimpleGrantedAuthority("ROLE_" + toRoleName(role));
            var auth = new UsernamePasswordAuthenticationToken(uid, null, List.of(authority));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        try {
            chain.doFilter(req, resp);
        } finally {
            // 防止线程复用造成污染
            SecurityContextHolder.clearContext();
        }
    }
}