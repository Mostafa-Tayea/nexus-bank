package com.mostafa.nexus_bank.cache.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {

    private static final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);

    private final Optional<RedisTemplate<String, Object>> redisTemplate;

    private static final String LOGIN_ATTEMPT_PREFIX = "login:attempt:";
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MINUTES = 15;

    @Autowired
    public LoginAttemptService(Optional<RedisTemplate<String, Object>> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void recordFailedAttempt(String email) {
        try {
            if (redisTemplate.isEmpty()) return;
            String key = LOGIN_ATTEMPT_PREFIX + email;
            Long attempts = redisTemplate.get().opsForValue().increment(key);
            if (attempts != null && attempts == 1) {
                redisTemplate.get().expire(key, LOCKOUT_DURATION_MINUTES, TimeUnit.MINUTES);
            }
            log.debug("Failed login attempt recorded for: {} (total: {})", email, attempts);
        } catch (Exception e) {
            log.error("Failed to record login attempt for: {}", email, e);
        }
    }

    public void clearFailedAttempts(String email) {
        try {
            if (redisTemplate.isEmpty()) return;
            redisTemplate.get().delete(LOGIN_ATTEMPT_PREFIX + email);
            log.debug("Failed login attempts cleared for: {}", email);
        } catch (Exception e) {
            log.error("Failed to clear login attempts for: {}", email, e);
        }
    }

    public int getFailedAttempts(String email) {
        try {
            if (redisTemplate.isEmpty()) return 0;
            Object value = redisTemplate.get().opsForValue().get(LOGIN_ATTEMPT_PREFIX + email);
            if (value instanceof Number number) {
                return number.intValue();
            }
        } catch (Exception e) {
            log.error("Failed to get login attempts for: {}", email, e);
        }
        return 0;
    }

    public boolean isLocked(String email) {
        return getFailedAttempts(email) >= MAX_ATTEMPTS;
    }
}
