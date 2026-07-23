package com.mostafa.nexus_bank.notification.service;

import com.mostafa.nexus_bank.common.enums.NotificationType;
import com.mostafa.nexus_bank.notification.dto.response.NotificationPageResponse;
import com.mostafa.nexus_bank.notification.entity.Notification;
import com.mostafa.nexus_bank.user.entity.User;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {

    Notification sendEmailNotification(User user, String title, String message);

    void sendOtpNotification(User user, String otpCode, NotificationType type);

    Notification createNotification(User user, String title, String message, NotificationType type);

    NotificationPageResponse getMyNotifications(UUID userId, Pageable pageable);

    Notification getNotificationById(UUID notificationId, UUID userId);

    NotificationPageResponse getMyUnreadNotifications(UUID userId, Pageable pageable);

    long getUnreadCount(UUID userId);

    void markAsRead(UUID notificationId, UUID userId);

    void markAllAsRead(UUID userId);

    void deleteNotification(UUID notificationId, UUID userId);
}
