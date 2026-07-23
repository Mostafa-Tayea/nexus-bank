package com.mostafa.nexus_bank.notification.listener;

import com.mostafa.nexus_bank.common.enums.NotificationType;
import com.mostafa.nexus_bank.common.event.*;
import com.mostafa.nexus_bank.notification.service.NotificationService;
import com.mostafa.nexus_bank.user.entity.User;
import com.mostafa.nexus_bank.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Async
    @EventListener
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("Processing UserRegisteredEvent for user: {}", event.email());
        User user = findUserById(event.userId());
        notificationService.createNotification(user,
                "Welcome to Nexus Bank",
                "Welcome " + event.fullName() + "! Your account has been created successfully. Please verify your email to start using our services.",
                NotificationType.IN_APP);
        log.debug("Welcome notification sent for user: {}", event.email());
    }

    @Async
    @EventListener
    public void handleUserLoggedIn(UserLoggedInEvent event) {
        log.info("Processing UserLoggedInEvent for user: {}", event.email());
        User user = findUserById(event.userId());
        notificationService.createNotification(user,
                "Login Successful",
                "You have successfully logged in at " + event.loginTime() + " from IP: " + event.ipAddress(),
                NotificationType.IN_APP);
        log.debug("Login notification sent for user: {}", event.email());
    }

    @Async
    @EventListener
    public void handlePasswordChanged(PasswordChangedEvent event) {
        log.info("Processing PasswordChangedEvent for user: {}", event.email());
        User user = findUserById(event.userId());
        notificationService.createNotification(user,
                "Password Changed",
                "Your password has been changed successfully. If you did not make this change, please contact support immediately.",
                NotificationType.EMAIL);
        log.debug("Password changed notification sent for user: {}", event.email());
    }

    @Async
    @EventListener
    public void handlePasswordReset(PasswordResetEvent event) {
        log.info("Processing PasswordResetEvent for user: {}", event.email());
        User user = findUserById(event.userId());
        notificationService.createNotification(user,
                "Password Reset",
                "Your password has been reset successfully. If you did not request this change, please contact support immediately.",
                NotificationType.EMAIL);
        log.debug("Password reset notification sent for user: {}", event.email());
    }

    @Async
    @EventListener
    public void handleEmailVerified(EmailVerifiedEvent event) {
        log.info("Processing EmailVerifiedEvent for user: {}", event.email());
        User user = findUserById(event.userId());
        notificationService.createNotification(user,
                "Email Verified",
                "Your email has been verified successfully. You can now access all features of your account.",
                NotificationType.IN_APP);
        log.debug("Email verified notification sent for user: {}", event.email());
    }

    @Async
    @EventListener
    public void handleMoneyDeposited(MoneyDepositedEvent event) {
        log.info("Processing MoneyDepositedEvent for account: {}", event.accountNumber());
        User user = findUserById(event.userId());
        notificationService.createNotification(user,
                "Money Deposited",
                "An amount of " + event.amount() + " has been deposited to your account " + event.accountNumber() + ". Reference: " + event.referenceNumber(),
                NotificationType.IN_APP);
        log.debug("Deposit notification sent for account: {}", event.accountNumber());
    }

    @Async
    @EventListener
    public void handleMoneyWithdrawn(MoneyWithdrawnEvent event) {
        log.info("Processing MoneyWithdrawnEvent for account: {}", event.accountNumber());
        User user = findUserById(event.userId());
        notificationService.createNotification(user,
                "Money Withdrawn",
                "An amount of " + event.amount() + " has been withdrawn from your account " + event.accountNumber() + ". Reference: " + event.referenceNumber(),
                NotificationType.IN_APP);
        log.debug("Withdrawal notification sent for account: {}", event.accountNumber());
    }

    @Async
    @EventListener
    public void handleMoneyTransferred(MoneyTransferredEvent event) {
        log.info("Processing MoneyTransferredEvent: {} from {} to {}", event.amount(), event.sourceAccountNumber(), event.destinationAccountNumber());

        User sourceUser = findUserById(event.sourceUserId());
        notificationService.createNotification(sourceUser,
                "Transfer Sent",
                "You have sent " + event.amount() + " from account " + event.sourceAccountNumber() + " to account " + event.destinationAccountNumber() + ". Reference: " + event.referenceNumber(),
                NotificationType.IN_APP);

        User destinationUser = findUserById(event.destinationUserId());
        notificationService.createNotification(destinationUser,
                "Transfer Received",
                "You have received " + event.amount() + " in account " + event.destinationAccountNumber() + " from account " + event.sourceAccountNumber() + ". Reference: " + event.referenceNumber(),
                NotificationType.IN_APP);

        log.debug("Transfer notifications sent for transaction: {}", event.referenceNumber());
    }

    @Async
    @EventListener
    public void handleTransactionFailed(TransactionFailedEvent event) {
        log.info("Processing TransactionFailedEvent for transaction: {}", event.referenceNumber());
        User user = findUserById(event.userId());
        notificationService.createNotification(user,
                "Transaction Failed",
                "Your " + event.type().toLowerCase() + " transaction of " + event.amount() + " has failed. Reason: " + event.reason() + ". Reference: " + event.referenceNumber(),
                NotificationType.IN_APP);
        log.debug("Transaction failed notification sent for: {}", event.referenceNumber());
    }

    @Async
    @EventListener
    public void handleAccountFrozen(AccountFrozenEvent event) {
        log.info("Processing AccountFrozenEvent for account: {}", event.accountNumber());
        User user = findUserById(event.userId());
        notificationService.createNotification(user,
                "Account Frozen",
                "Your account " + event.accountNumber() + " has been frozen. Reason: " + event.reason() + ". Please contact support for more information.",
                NotificationType.EMAIL);
        log.debug("Account frozen notification sent for account: {}", event.accountNumber());
    }

    @Async
    @EventListener
    public void handleAccountClosed(AccountClosedEvent event) {
        log.info("Processing AccountClosedEvent for account: {}", event.accountNumber());
        User user = findUserById(event.userId());
        notificationService.createNotification(user,
                "Account Closed",
                "Your account " + event.accountNumber() + " has been closed. Reason: " + event.reason() + ". Please contact support for more information.",
                NotificationType.EMAIL);
        log.debug("Account closed notification sent for account: {}", event.accountNumber());
    }

    @Async
    @EventListener
    public void handleOtpGenerated(OtpGeneratedEvent event) {
        log.info("Processing OtpGeneratedEvent for user: {}", event.email());
        User user = findUserById(event.userId());
        notificationService.createNotification(user,
                "Verification Code",
                "Your verification code is: " + event.otpCode() + ". This code expires in 10 minutes.",
                event.notificationType());
        log.debug("OTP notification sent for user: {}", event.email());
    }

    private User findUserById(java.util.UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new com.mostafa.nexus_bank.exception.EntityNotFoundException("User", "id", userId));
    }
}
