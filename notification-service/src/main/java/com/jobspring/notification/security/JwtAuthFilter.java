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

        if (auth == null || !auth.startsWith("Bearer ")) {
            System.err.println("[notification] No Bearer token");
            w.setStatus(401);
            return;
        }

        try {
            String token = auth.substring(7);
            jwt.verify(token);
            chain.doFilter(req, res);
        } catch (Exception e) {
            e.printStackTrace();
            w.setStatus(401);
        }
    }
}