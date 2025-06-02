package com.example.notificationservice.infrastructure.service;

import com.example.notificationservice.infrastructure.util.IdempotencyKeyGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final StringRedisTemplate redisTemplate;
    private final IdempotencyKeyGenerator keyGenerator;
    private static final long TTL_SECONDS = 60;

    public boolean isDuplicate(Object payload) {
        String key = keyGenerator.generateKey(payload);
        Boolean isNewKey = redisTemplate.opsForValue().setIfAbsent(key, "1", TTL_SECONDS, TimeUnit.SECONDS);
        return isNewKey == null || !isNewKey;
    }
} 