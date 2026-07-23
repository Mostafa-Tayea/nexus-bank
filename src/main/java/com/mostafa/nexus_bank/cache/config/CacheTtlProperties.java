package com.mostafa.nexus_bank.cache.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "cache.ttl")
public class CacheTtlProperties {

    private long user = 3600;
    private long account = 1800;
    private long role = 7200;
    private long otp = 900;
    private long refreshToken = 600;
    private long notification = 900;
    private long notificationCount = 300;
    private long jwtBlacklist = 3600;
}
