package com.mostafa.nexus_bank.scheduler.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "scheduler")
public class SchedulerProperties {

    private Cleanup cleanup = new Cleanup();

    @Getter
    @Setter
    public static class Cleanup {
        private String otpCron = "0 0 3 * * ?";
        private String verificationTokenCron = "0 0 3 * * ?";
        private String refreshTokenCron = "0 0 2 * * ?";
        private String notificationCron = "0 0 4 * * ?";
        private String auditCron = "0 0 4 * * ?";
        private String transferLimitResetCron = "0 0 0 * * ?";
        private String cacheCleanupCron = "0 0 1 * * ?";

        private int notificationRetentionDays = 90;
        private int auditRetentionDays = 365;
    }
}
