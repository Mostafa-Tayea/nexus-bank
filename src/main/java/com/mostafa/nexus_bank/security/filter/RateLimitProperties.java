package com.mostafa.nexus_bank.security.filter;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {

    private Map<String, EndpointLimit> endpoints = new HashMap<>();

    @Getter
    @Setter
    public static class EndpointLimit {
        private long capacity = 10;
        private long refillTokens = 10;
        private long refillDurationSeconds = 60;
    }
}
