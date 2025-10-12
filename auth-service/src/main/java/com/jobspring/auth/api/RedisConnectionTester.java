package com.jobspring.auth.api;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisConnectionTester {
    private final StringRedisTemplate redisTemplate;

    @PostConstruct
    public void testRedis() {
        try {
            redisTemplate.opsForValue().set("test:ping", "pong");
            String result = redisTemplate.opsForValue().get("test:ping");
            System.out.println("✅ Redis connected successfully. Value: " + result);
        } catch (Exception e) {
            System.err.println("❌ Redis connection failed: " + e.getMessage());
        }
    }
}
