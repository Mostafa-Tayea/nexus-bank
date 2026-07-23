package com.mostafa.nexus_bank.cache.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class JwtBlacklistService {

    private static final Logger log = LoggerFactory.getLogger(JwtBlacklistService.class);

    private final Optional<RedisTemplate<String, Object>> redisTemplate;

    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    @Autowired
    public JwtBlacklistService(Optional<RedisTemplate<String, Object>> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void blacklistToken(String token, long expirationMillis) {
        try {
            if (redisTemplate.isEmpty()) return;
            long ttlSeconds = expirationMillis / 1000;
            if (ttlSeconds > 0) {
                redisTemplate.get().opsForValue().set(BLACKLIST_PREFIX + token, "revoked", ttlSeconds, TimeUnit.SECONDS);
                log.debug("Token blacklisted with TTL: {} seconds", ttlSeconds);
            }
        } catch (Exception e) {
            log.error("Failed to blacklist token", e);
        }
    }

    public boolean isTokenBlacklisted(String token) {
        try {
            if (redisTemplate.isEmpty()) return false;
            Boolean exists = redisTemplate.get().hasKey(BLACKLIST_PREFIX + token);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("Failed to check token blacklist", e);
            return false;
        }
    }
}
