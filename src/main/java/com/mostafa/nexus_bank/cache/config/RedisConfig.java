package com.mostafa.nexus_bank.cache.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
                "users", "accounts", "roles", "otp", "refreshTokens",
                "notifications", "notificationCount", "jwtBlacklist"
        );
    }

    @Bean
    @ConditionalOnBean(RedisConnectionFactory.class)
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory,
                                          CacheTtlProperties cacheTtlProperties) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL);

        Jackson2JsonRedisSerializer<Object> jsonSerializer =
                new Jackson2JsonRedisSerializer<>(mapper, Object.class);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(cacheTtlProperties.getJwtBlacklist()))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(jsonSerializer))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("users",
                defaultConfig.entryTtl(Duration.ofSeconds(cacheTtlProperties.getUser())));
        cacheConfigurations.put("accounts",
                defaultConfig.entryTtl(Duration.ofSeconds(cacheTtlProperties.getAccount())));
        cacheConfigurations.put("roles",
                defaultConfig.entryTtl(Duration.ofSeconds(cacheTtlProperties.getRole())));
        cacheConfigurations.put("otp",
                defaultConfig.entryTtl(Duration.ofSeconds(cacheTtlProperties.getOtp())));
        cacheConfigurations.put("refreshTokens",
                defaultConfig.entryTtl(Duration.ofSeconds(cacheTtlProperties.getRefreshToken())));
        cacheConfigurations.put("notifications",
                defaultConfig.entryTtl(Duration.ofSeconds(cacheTtlProperties.getNotification())));
        cacheConfigurations.put("notificationCount",
                defaultConfig.entryTtl(Duration.ofSeconds(cacheTtlProperties.getNotificationCount())));
        cacheConfigurations.put("jwtBlacklist",
                defaultConfig.entryTtl(Duration.ofSeconds(cacheTtlProperties.getJwtBlacklist())));

        return org.springframework.data.redis.cache.RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }

    @Bean
    @ConditionalOnBean(RedisConnectionFactory.class)
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        ObjectMapper objectMapper = new ObjectMapper();
        Jackson2JsonRedisSerializer<Object> jsonSerializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        template.afterPropertiesSet();

        return template;
    }
}
