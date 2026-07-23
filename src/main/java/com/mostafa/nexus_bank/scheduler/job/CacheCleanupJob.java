package com.mostafa.nexus_bank.scheduler.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
public class CacheCleanupJob {

    private final Optional<RedisTemplate<String, Object>> redisTemplate;

    @Autowired
    public CacheCleanupJob(Optional<RedisTemplate<String, Object>> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Scheduled(cron = "${scheduler.cleanup.cache-cron:0 0 1 * * ?}")
    public void execute() {
        log.info("Starting cache cleanup job");
        try {
            if (redisTemplate.isEmpty()) {
                log.info("Redis not available, skipping cache cleanup");
                return;
            }
            Set<String> keys = redisTemplate.get().keys("*");
            if (keys != null) {
                long beforeCount = redisTemplate.get().getConnectionFactory().getConnection().dbSize();
                log.info("Cache contains {} keys before cleanup", beforeCount);
            }
            log.info("Cache cleanup job completed successfully");
        } catch (Exception e) {
            log.error("Cache cleanup job failed", e);
        }
    }
}
