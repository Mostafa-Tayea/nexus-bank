package com.mostafa.nexus_bank.scheduler.job;

import com.mostafa.nexus_bank.auth.repository.OtpCodeRepository;
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
public class ExpiredOtpCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(ExpiredOtpCleanupJob.class);

    private final OtpCodeRepository otpCodeRepository;
    private final SchedulerProperties schedulerProperties;

    @Scheduled(cron = "${scheduler.cleanup.otp-cron:0 0 3 * * ?}")
    @Transactional
    public void execute() {
        log.info("Starting expired OTP cleanup job");
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
            otpCodeRepository.deleteByExpiryTimeBefore(cutoff);
            log.info("Expired OTP cleanup job completed successfully");
        } catch (Exception e) {
            log.error("Expired OTP cleanup job failed", e);
        }
    }
}
