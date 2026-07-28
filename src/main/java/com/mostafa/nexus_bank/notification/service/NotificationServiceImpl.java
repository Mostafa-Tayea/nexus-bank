package com.mostafa.nexus_bank.notification.service;

import com.mostafa.nexus_bank.cache.config.CacheNames;
import com.mostafa.nexus_bank.common.enums.NotificationStatus;
import com.mostafa.nexus_bank.common.enums.NotificationType;
import com.mostafa.nexus_bank.exception.EntityNotFoundException;
import com.mostafa.nexus_bank.notification.dto.response.NotificationPageResponse;
import com.mostafa.nexus_bank.notification.dto.response.NotificationResponse;
import com.mostafa.nexus_bank.notification.entity.Notification;
import com.mostafa.nexus_bank.notification.mapper.NotificationMapper;
import com.mostafa.nexus_bank.notification.repository.NotificationRepository;
import com.mostafa.nexus_bank.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final EmailService emailService;

    @Override
    @Transactional
    public Notification sendEmailNotification(User user, String title, String message) {
        return createNotification(user, title, message, NotificationType.EMAIL);
    }

    @Override
    @Transactional
    public void sendOtpNotification(User user, String otpCode, NotificationType type) {
        String title = switch (type) {
            case EMAIL -> "Email Verification Code";
            case SMS -> "SMS Verification Code";
            case PUSH -> "Push Notification";
            case IN_APP -> "In-App Notification";
        };

        createNotification(user, title,
                "Your verification code is: " + otpCode + ". This code expires in 10 minutes.", type);
    }

    @Override
    @Transactional
    public Notification createNotification(User user, String title, String message, NotificationType type) {
        log.debug("Creating {} notification for user: {}", type, user.getEmail());

        Notification notification = Notification.builder()
                .title(title)
                .message(message)
                .type(type)
                .status(NotificationStatus.UNREAD)
                .user(user)
                .build();

        Notification saved = notificationRepository.save(notification);

        if (type == NotificationType.EMAIL) {
            try {
                String htmlBody = buildEmailHtml(title, message);
                emailService.sendEmail(user.getEmail(), title, htmlBody);
                saved.setStatus(NotificationStatus.SENT);
                saved.setSentAt(LocalDateTime.now());
                log.debug("Email notification sent to user: {}", user.getEmail());
            } catch (Exception e) {
                saved.setStatus(NotificationStatus.FAILED);
                saved.setFailureReason(e.getMessage());
                saved.setRetryCount(saved.getRetryCount() + 1);
                log.error("Failed to send email notification to user: {}", user.getEmail(), e);
            }
        } else {
            saved.setStatus(NotificationStatus.SENT);
            saved.setSentAt(LocalDateTime.now());
        }

        return notificationRepository.save(saved);
    }

    private String buildEmailHtml(String title, String message) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }
                        .container { max-width: 600px; margin: 20px auto; background: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
                        .header { background: #1a237e; color: #ffffff; padding: 24px; text-align: center; }
                        .header h1 { margin: 0; font-size: 24px; }
                        .body { padding: 32px; color: #333333; line-height: 1.6; }
                        .body h2 { color: #1a237e; margin-top: 0; }
                        .footer { background: #f4f4f4; color: #888888; padding: 16px; text-align: center; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header"><h1>Nexus Bank</h1></div>
                        <div class="body">
                            <h2>%s</h2>
                            <p>%s</p>
                        </div>
                        <div class="footer">&copy; 2026 Nexus Bank. All rights reserved.</div>
                    </div>
                </body>
                </html>
                """.formatted(title, message);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationPageResponse getMyNotifications(UUID userId, Pageable pageable) {
        Page<Notification> page = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        long unreadCount = notificationRepository.countByUserIdAndStatusNot(userId, NotificationStatus.READ);

        List<NotificationResponse> content = page.getContent().stream()
                .map(notificationMapper::toResponse)
                .toList();

        return NotificationPageResponse.builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .unreadCount(unreadCount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Notification getNotificationById(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification", "id", notificationId));

        if (!notification.getUser().getId().equals(userId)) {
            throw new com.mostafa.nexus_bank.exception.ForbiddenException("You do not have access to this notification");
        }

        return notification;
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationPageResponse getMyUnreadNotifications(UUID userId, Pageable pageable) {
        Page<Notification> page = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        long unreadCount = notificationRepository.countByUserIdAndStatusNot(userId, NotificationStatus.READ);

        List<NotificationResponse> content = page.getContent().stream()
                .filter(n -> n.getStatus() != NotificationStatus.READ)
                .map(notificationMapper::toResponse)
                .toList();

        return NotificationPageResponse.builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .unreadCount(unreadCount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.NOTIFICATION_COUNT, key = "#userId", unless = "#result < 0")
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndStatusNot(userId, NotificationStatus.READ);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheNames.NOTIFICATION_COUNT, key = "#userId")
    public void markAsRead(UUID notificationId, UUID userId) {
        Notification notification = getNotificationById(notificationId, userId);

        if (notification.getStatus() != NotificationStatus.READ) {
            notification.setStatus(NotificationStatus.READ);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
            log.debug("Notification marked as read: {}", notificationId);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheNames.NOTIFICATION_COUNT, key = "#userId")
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsRead(userId);
        log.debug("All notifications marked as read for user: {}", userId);
    }

    @Override
    @Transactional
    public void deleteNotification(UUID notificationId, UUID userId) {
        Notification notification = getNotificationById(notificationId, userId);
        notificationRepository.delete(notification);
        log.debug("Notification deleted: {}", notificationId);
    }
}
