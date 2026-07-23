package com.mostafa.nexus_bank.notification.repository;

import com.mostafa.nexus_bank.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByUserId(UUID userId);

    Page<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    long countByUserIdAndStatusNot(UUID userId, com.mostafa.nexus_bank.common.enums.NotificationStatus status);

    @Modifying
    @Query("UPDATE Notification n SET n.status = com.mostafa.nexus_bank.common.enums.NotificationStatus.READ, n.readAt = CURRENT_TIMESTAMP WHERE n.user.id = :userId AND n.status <> com.mostafa.nexus_bank.common.enums.NotificationStatus.READ")
    void markAllAsRead(@Param("userId") UUID userId);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoff")
    void deleteByCreatedAtBefore(@Param("cutoff") LocalDateTime cutoff);
}
