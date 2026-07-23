package com.mostafa.nexus_bank.security.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter implements Ordered {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private final RateLimitProperties rateLimitProperties;
    private final Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String endpoint = resolveEndpoint(request);

        if (endpoint != null) {
            Bucket bucket = bucketCache.computeIfAbsent(endpoint, this::createBucket);
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

            if (probe.isConsumed()) {
                response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
                filterChain.doFilter(request, response);
            } else {
                long waitTimeSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;
                response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitTimeSeconds));
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                String jsonResponse = """
                        {"success":false,"message":"Rate limit exceeded. Please try again later.","timestamp":"%s"}
                        """.formatted(java.time.LocalDateTime.now());
                response.getWriter().write(jsonResponse);
                log.warn("Rate limit exceeded for endpoint: {} from IP: {}", endpoint, request.getRemoteAddr());
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private String resolveEndpoint(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        if (uri.startsWith("/api/v1/auth/")) {
            if ("POST".equals(method)) {
                if (uri.contains("/register")) return "auth.register";
                if (uri.contains("/login")) return "auth.login";
                if (uri.contains("/forgot-password")) return "auth.forgot-password";
                if (uri.contains("/reset-password")) return "auth.reset-password";
                if (uri.contains("/verify-otp")) return "auth.verify-otp";
                if (uri.contains("/refresh-token")) return "auth.refresh-token";
                if (uri.contains("/verify-email")) return "auth.verify-email";
            }
        }

        if (uri.startsWith("/api/v1/transactions/")) {
            if ("POST".equals(method)) {
                if (uri.contains("/deposit")) return "transaction.deposit";
                if (uri.contains("/withdraw")) return "transaction.withdraw";
                if (uri.contains("/transfer")) return "transaction.transfer";
            }
        }

        if (uri.startsWith("/api/v1/accounts") && "POST".equals(method)) {
            return "account.create";
        }

        return null;
    }

    private Bucket createBucket(String endpoint) {
        RateLimitProperties.EndpointLimit limit = rateLimitProperties.getEndpoints()
                .getOrDefault(endpoint, createDefaultLimit());

        Bandwidth bandwidth = Bandwidth.classic(
                limit.getCapacity(),
                Refill.greedy(limit.getRefillTokens(), Duration.ofSeconds(limit.getRefillDurationSeconds()))
        );

        return Bucket.builder().addLimit(bandwidth).build();
    }

    private RateLimitProperties.EndpointLimit createDefaultLimit() {
        RateLimitProperties.EndpointLimit limit = new RateLimitProperties.EndpointLimit();
        limit.setCapacity(10);
        limit.setRefillTokens(10);
        limit.setRefillDurationSeconds(60);
        return limit;
    }
}
