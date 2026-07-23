package com.mostafa.nexus_bank.audit.listener;

import com.mostafa.nexus_bank.audit.entity.Audit;
import com.mostafa.nexus_bank.audit.service.AuditService;
import com.mostafa.nexus_bank.common.event.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuditListener {

    private static final Logger log = LoggerFactory.getLogger(AuditListener.class);

    private final AuditService auditService;

    @Async
    @EventListener
    public void handleUserRegistered(UserRegisteredEvent event) {
        saveAuditLog("USER_REGISTRATION", "User registered: " + event.email(),
                event.email(), event.userId(), null, "SUCCESS",
                "New user registered with email: " + event.email());
    }

    @Async
    @EventListener
    public void handleUserLoggedIn(UserLoggedInEvent event) {
        saveAuditLog("USER_LOGIN", "User logged in: " + event.email(),
                event.email(), event.userId(), event.ipAddress(), "SUCCESS",
                "Login from IP: " + event.ipAddress() + ", Device: " + event.device());
    }

    @Async
    @EventListener
    public void handlePasswordChanged(PasswordChangedEvent event) {
        saveAuditLog("PASSWORD_CHANGED", "Password changed for user: " + event.email(),
                event.email(), event.userId(), null, "SUCCESS",
                "Password changed successfully");
    }

    @Async
    @EventListener
    public void handlePasswordReset(PasswordResetEvent event) {
        saveAuditLog("PASSWORD_RESET", "Password reset for user: " + event.email(),
                event.email(), event.userId(), null, "SUCCESS",
                "Password reset successfully");
    }

    @Async
    @EventListener
    public void handleEmailVerified(EmailVerifiedEvent event) {
        saveAuditLog("EMAIL_VERIFIED", "Email verified for user: " + event.email(),
                event.email(), event.userId(), null, "SUCCESS",
                "Email verified successfully");
    }

    @Async
    @EventListener
    public void handleMoneyDeposited(MoneyDepositedEvent event) {
        saveAuditLog("MONEY_DEPOSITED", "Money deposited to account: " + event.accountNumber(),
                null, event.userId(), null, "SUCCESS",
                "Amount: " + event.amount() + ", Reference: " + event.referenceNumber());
    }

    @Async
    @EventListener
    public void handleMoneyWithdrawn(MoneyWithdrawnEvent event) {
        saveAuditLog("MONEY_WITHDRAWN", "Money withdrawn from account: " + event.accountNumber(),
                null, event.userId(), null, "SUCCESS",
                "Amount: " + event.amount() + ", Reference: " + event.referenceNumber());
    }

    @Async
    @EventListener
    public void handleMoneyTransferred(MoneyTransferredEvent event) {
        saveAuditLog("MONEY_TRANSFERRED", "Money transferred from " + event.sourceAccountNumber() + " to " + event.destinationAccountNumber(),
                null, event.sourceUserId(), null, "SUCCESS",
                "Amount: " + event.amount() + ", Reference: " + event.referenceNumber());
    }

    @Async
    @EventListener
    public void handleTransactionCompleted(TransactionCompletedEvent event) {
        saveAuditLog("TRANSACTION_COMPLETED", "Transaction completed: " + event.referenceNumber(),
                null, event.userId(), null, "SUCCESS",
                "Type: " + event.type() + ", Amount: " + event.amount());
    }

    @Async
    @EventListener
    public void handleTransactionFailed(TransactionFailedEvent event) {
        saveAuditLog("TRANSACTION_FAILED", "Transaction failed: " + event.referenceNumber(),
                null, event.userId(), null, "FAILED",
                "Type: " + event.type() + ", Amount: " + event.amount() + ", Reason: " + event.reason());
    }

    @Async
    @EventListener
    public void handleAccountCreated(AccountCreatedEvent event) {
        saveAuditLog("ACCOUNT_CREATED", "Account created: " + event.accountNumber(),
                null, event.userId(), null, "SUCCESS",
                "Account type: " + event.accountType());
    }

    @Async
    @EventListener
    public void handleAccountFrozen(AccountFrozenEvent event) {
        saveAuditLog("ACCOUNT_FROZEN", "Account frozen: " + event.accountNumber(),
                null, event.userId(), null, "SUCCESS",
                "Reason: " + event.reason());
    }

    @Async
    @EventListener
    public void handleAccountClosed(AccountClosedEvent event) {
        saveAuditLog("ACCOUNT_CLOSED", "Account closed: " + event.accountNumber(),
                null, event.userId(), null, "SUCCESS",
                "Reason: " + event.reason());
    }

    private void saveAuditLog(String eventType, String action, String username,
                              UUID userId, String ipAddress, String result, String details) {
        try {
            Audit audit = Audit.builder()
                    .eventType(eventType)
                    .action(action)
                    .username(username)
                    .userId(userId)
                    .ipAddress(ipAddress)
                    .timestamp(LocalDateTime.now())
                    .result(result)
                    .details(details)
                    .build();

            auditService.saveAudit(audit);
            log.debug("Audit log saved: {} - {}", eventType, action);
        } catch (Exception e) {
            log.error("Failed to save audit log for event: {}", eventType, e);
        }
    }
}
