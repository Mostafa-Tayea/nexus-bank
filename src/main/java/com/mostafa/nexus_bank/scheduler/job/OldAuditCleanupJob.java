package com.mostafa.nexus_bank.scheduler.job;

import com.mostafa.nexus_bank.audit.repository.AuditRepository;
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
public class OldAuditCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(OldAuditCleanupJob.class);

    private final AuditRepository auditRepository;
    private final SchedulerProperties schedulerProperties;

    @Scheduled(cron = "${scheduler.cleanup.audit-cron:0 0 4 * * ?}")
    @Transactional
    public void execute() {
        log.info("Starting old audit log cleanup job");
        try {
            int retentionDays = schedulerProperties.getCleanup().getAuditRetentionDays();
            LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);

            auditRepository.deleteByTimestampBefore(cutoff);

            log.info("Old audit log cleanup job completed. Retention: {} days", retentionDays);
        } catch (Exception e) {
            log.error("Old audit log cleanup job failed", e);
        }
    }
}
