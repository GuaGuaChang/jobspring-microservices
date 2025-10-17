package com.jobspring.notification.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.IOException;

public class JwtAuthFilter implements Filter {
    private final JwtService jwt;

    public JwtAuthFilter(JwtService jwt) {
        this.jwt = jwt;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest r = (HttpServletRequest) req;
        HttpServletResponse w = (HttpServletResponse) res;

        String auth = r.getHeader("Authorization");
        System.out.println("🔍 [notification] Received Auth header: " +
                (auth != null ? auth.substring(0, Math.min(30, auth.length())) + "..." : "null"));

        if (auth == null || !auth.startsWith("Bearer ")) {
            System.err.println("❌ [notification] No Bearer token");
            w.setStatus(401);
            return;
        }

        try {
            String token = auth.substring(7);
            System.out.println("🔓 [notification] Verifying token: " +
                    token.substring(0, Math.min(20, token.length())) + "...");
            jwt.verify(token);
            System.out.println("✅ [notification] Token verified");
            chain.doFilter(req, res);
        } catch (Exception e) {
            System.err.println("❌ [notification] JWT verification failed: " + e.getClass().getName());
            System.err.println("❌ Message: " + e.getMessage());
            e.printStackTrace();
            w.setStatus(401);
        }
    }
}