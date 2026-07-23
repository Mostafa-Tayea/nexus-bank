package com.mostafa.nexus_bank.scheduler.job;

import com.mostafa.nexus_bank.auth.repository.RefreshTokenRepository;
import com.mostafa.nexus_bank.scheduler.config.SchedulerProperties;
import com.mostafa.nexus_bank.security.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ExpiredRefreshTokenCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(ExpiredRefreshTokenCleanupJob.class);

    private final RefreshTokenService refreshTokenService;
    private final SchedulerProperties schedulerProperties;

    @Scheduled(cron = "${scheduler.cleanup.refresh-token-cron:0 0 2 * * ?}")
    @Transactional
    public void execute() {
        log.info("Starting expired refresh token cleanup job");
        try {
            refreshTokenService.deleteExpiredTokens();
            log.info("Expired refresh token cleanup job completed successfully");
        } catch (Exception e) {
            log.error("Expired refresh token cleanup job failed", e);
        }
    }
}
