package com.jobspring.auth.api;

import com.jobspring.auth.account.Account;
import com.jobspring.auth.account.AccountRepo;
import com.jobspring.auth.dto.*;
import com.jobspring.auth.service.VerificationService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final VerificationService verificationService;

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
        verificationService.verifyOrThrow(req.getEmail(), req.getCode());
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
        if (!Boolean.TRUE.equals(a.getActive()) || !encoder.matches(req.getPassword(), a.getPasswordHash())) {
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

        return new AuthResponse(
                new UserDTO(a.getId(), a.getEmail(), a.getFullName(), a.getRole()),
                token,
                exp.toEpochMilli()
        );
    }

    @GetMapping("/me")
    public MeResp me(@RequestHeader(value = "X-User-Id", required = false) String uid) {
        if (uid == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        var a = accounts.findById(Long.parseLong(uid))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return new MeResp(a.getId(), a.getEmail(), a.getFullName(), a.getRole());
    }

    @PostMapping("/send-code")
    public ResponseEntity<Void> sendCode(@Valid @RequestBody SendCodeRequestDTO req) {
        verificationService.sendRegisterCode(req.getEmail());
        return ResponseEntity.noContent().build();
    }

    @Data
    public static class IdResp {
        private final Long id;
    }

    public record MeResp(Long id, String email, String fullName, Byte role) {
    }
}