package com.jobspring.notification.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

public class JwtService {
    private final SecretKey key;

    public JwtService(String secret) {
        System.out.println("[JWT] rawLen=" + secret.length() +
                " head=" + (secret.length() > 12 ? secret.substring(0, 12) : secret));
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Jws<Claims> verify(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .setAllowedClockSkewSeconds(60)
                .build()
                .parseClaimsJws(token);
    }
}