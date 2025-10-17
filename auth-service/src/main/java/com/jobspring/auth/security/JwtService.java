package com.jobspring.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

public class JwtService {
    private final SecretKey key;

    public JwtService(String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String issueInternalToken(String caller, int ttlMinutes) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject("system")
                .claim("svc", caller)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(ttlMinutes * 60L)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}