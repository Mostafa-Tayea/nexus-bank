package com.mostafa.nexus_bank.scheduler.job;

import com.mostafa.nexus_bank.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DailyTransferLimitResetJob {

    private static final Logger log = LoggerFactory.getLogger(DailyTransferLimitResetJob.class);

    private final AccountRepository accountRepository;

    @Scheduled(cron = "${scheduler.cleanup.transfer-limit-reset-cron:0 0 0 * * ?}")
    @Transactional
    public void execute() {
        log.info("Starting daily transfer limit reset job");
        try {
            accountRepository.resetAllDailyTransferredAmounts();
            log.info("Daily transfer limit reset job completed successfully");
        } catch (Exception e) {
            log.error("Daily transfer limit reset job failed", e);
        }
    }
}
