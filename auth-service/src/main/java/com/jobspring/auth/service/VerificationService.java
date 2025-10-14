package com.jobspring.auth.service;

import com.jobspring.auth.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;


@Service
@RequiredArgsConstructor
public class VerificationService {

    private final StringRedisTemplate redis;

    @Value("${security.verification.expMinutes:10}")
    private int expMinutes;


    @Value("${security.verification.maxAttempts:5}")
    private int maxAttempts;


    public void verifyOrThrow(String email, String code) {
        String codeKey = "verify:register:" + email;
        String attemptKey = "verify:attempt:" + email;

        String hash = redis.opsForValue().get(codeKey);
        if (hash == null) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "Invalid or expired verification code.");
        }

        String attemptStr = redis.opsForValue().get(attemptKey);
        int attempts = attemptStr == null ? 0 : Integer.parseInt(attemptStr);
        if (attempts >= maxAttempts) {
            redis.delete(codeKey);
            throw new BizException(ErrorCode.TOO_MANY_REQUESTS, "Too many attempts. Request a new code.");
        }

        if (!HashUtils.matches(code, hash)) {
            redis.opsForValue().increment(attemptKey);
            redis.expire(attemptKey, Duration.ofMinutes(expMinutes));
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "Invalid verification code.");
        }

        // 验证成功后删除
        redis.delete(codeKey);
        redis.delete(attemptKey);
    }
}

