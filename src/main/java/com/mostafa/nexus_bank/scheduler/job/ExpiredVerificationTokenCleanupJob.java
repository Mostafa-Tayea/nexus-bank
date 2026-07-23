package com.mostafa.nexus_bank.scheduler.job;

import com.mostafa.nexus_bank.auth.repository.VerificationTokenRepository;
import com.mostafa.nexus_bank.scheduler.config.SchedulerProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ExpiredVerificationTokenCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(ExpiredVerificationTokenCleanupJob.class);

    private final VerificationTokenRepository verificationTokenRepository;
    private final SchedulerProperties schedulerProperties;

    @Scheduled(cron = "${scheduler.cleanup.verification-token-cron:0 0 3 * * ?}")
    @Transactional
    public void execute() {
        log.info("Starting expired verification token cleanup job");
        try {
            verificationTokenRepository.deleteVerifiedAndExpiredBefore(LocalDateTime.now());
            log.info("Expired verification token cleanup job completed successfully");
        } catch (Exception e) {
            log.error("Expired verification token cleanup job failed", e);
        }
    }
}
