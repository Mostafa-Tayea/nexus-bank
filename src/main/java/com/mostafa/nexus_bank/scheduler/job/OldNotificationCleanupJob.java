package com.mostafa.nexus_bank.scheduler.job;

import com.mostafa.nexus_bank.notification.repository.NotificationRepository;
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
public class OldNotificationCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(OldNotificationCleanupJob.class);

    private final NotificationRepository notificationRepository;
    private final SchedulerProperties schedulerProperties;

    @Scheduled(cron = "${scheduler.cleanup.notification-cron:0 0 4 * * ?}")
    @Transactional
    public void execute() {
        log.info("Starting old notification cleanup job");
        try {
            int retentionDays = schedulerProperties.getCleanup().getNotificationRetentionDays();
            LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);

            notificationRepository.deleteByCreatedAtBefore(cutoff);

            log.info("Old notification cleanup job completed. Retention: {} days", retentionDays);
        } catch (Exception e) {
            log.error("Old notification cleanup job failed", e);
        }
    }
}
