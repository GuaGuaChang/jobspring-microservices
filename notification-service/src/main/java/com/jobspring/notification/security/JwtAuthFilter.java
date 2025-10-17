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
            w.setStatus(401);
            return;
        }
        try {
            jwt.verify(auth.substring(7));
            chain.doFilter(req, res);
        } catch (Exception e) {
            w.setStatus(401);
        }
    }
}