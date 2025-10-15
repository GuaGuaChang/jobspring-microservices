package com.jobspring.notification.service;

import brave.internal.Nullable;
import com.jobspring.notification.util.BizException;
import com.jobspring.notification.util.CodeGenerator;
import com.jobspring.notification.util.ErrorCode;
import com.jobspring.notification.util.HashUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;


@Service
@RequiredArgsConstructor
public class VerificationService {

    @Autowired(required = false)
    @Nullable
    private StringRedisTemplate redis;
    private final MailService mail;

    @Value("${security.verification.expMinutes:10}")
    private int expMinutes;

    @Value("${security.verification.cooldownSeconds:60}")
    private int cooldownSeconds;

    @Value("${security.verification.dailyLimit:10}")
    private int dailyLimit;

    @Value("${security.verification.maxAttempts:5}")
    private int maxAttempts;

    public void sendRegisterCode(String email) {
        String codeKey = "verify:register:" + email;
        String timeKey = "verify:lastsent:" + email;
        String countKey = "verify:dailycount:" + email;

        // 限制每天发送次数
        String countStr = redis.opsForValue().get(countKey);
        int count = countStr == null ? 0 : Integer.parseInt(countStr);
        if (count >= dailyLimit) {
            throw new BizException(ErrorCode.TOO_MANY_REQUESTS, "Too many requests today.");
        }

        // 冷却时间检查
        if (redis.hasKey(timeKey)) {
            throw new BizException(ErrorCode.TOO_MANY_REQUESTS, "Please wait before requesting another code.");
        }

        // 生成验证码
        String code = CodeGenerator.numeric6();
        String hash = HashUtils.hash(code);

        // 保存验证码，设置过期时间
        redis.opsForValue().set(codeKey, hash, Duration.ofMinutes(expMinutes));

        // 冷却时间标记
        redis.opsForValue().set(timeKey, "1", Duration.ofSeconds(cooldownSeconds));

        // 更新每日计数
        redis.opsForValue().increment(countKey);
        redis.expire(countKey, Duration.ofDays(1));

        // 发送邮件
        mail.sendPlainText(
                email,
                "Your JobSpring verification code",
                "Your verification code is: " + code + "\nIt expires in " + expMinutes + " minutes."
        );
    }

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

