package com.jobspring.auth.api;

import com.jobspring.auth.account.Account;
import com.jobspring.auth.account.AccountRepo;
import com.jobspring.auth.dto.AuthResponse;
import com.jobspring.auth.dto.LoginRequest;
import com.jobspring.auth.dto.RegisterRequest;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AccountRepo accounts;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.ttl-minutes}")
    private long ttlMinutes;

    @PostMapping("/register")
    public IdResp register(@Valid @RequestBody RegisterRequest req) {
        accounts.findByEmail(req.getEmail())
                .ifPresent(a -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "email exists");
                });

        var a = new Account();
        a.setEmail(req.getEmail());
        a.setFullName(req.getFullName());
        a.setPasswordHash(encoder.encode(req.getPassword()));
        a.setRole((byte) 0);
        a.setActive(true);
        a = accounts.save(a);
        return new IdResp(a.getId());
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        var a = accounts.findByEmail(req.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        if (!Boolean.TRUE.equals(a.getActive()) ||
                !encoder.matches(req.getPassword(), a.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        var now = Instant.now();
        var exp = now.plus(Duration.ofMinutes(ttlMinutes));
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .claim("uid", a.getId())
                .claim("role", a.getRole())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return new AuthResponse(a.getId(), a.getEmail(), a.getFullName(), a.getRole(), token, exp.toEpochMilli());
    }

    @GetMapping("/me")
    public MeResp me(@RequestHeader(value = "X-User-Id", required = false) String uid) {
        if (uid == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        var a = accounts.findById(Long.parseLong(uid))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return new MeResp(a.getId(), a.getEmail(), a.getFullName(), a.getRole());
    }

    @Data
    public static class RegisterReq {
        @Email
        @NotBlank
        private String email;
        @NotBlank
        private String password;
        private String phone;
        @NotBlank
        private String fullName;
    }

    @Data
    public static class LoginReq {
        @Email
        @NotBlank
        private String email;
        @NotBlank
        private String password;
    }

    @Data
    public static class TokenResp {
        private final String token;
        private final long expiresAt;
    }

    @Data
    public static class IdResp {
        private final Long id;
    }

    public record MeResp(Long id, String email, String fullName, Byte role) {
    }
}