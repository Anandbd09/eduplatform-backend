package com.eduplatform.notification.service;

import com.eduplatform.notification.model.NotificationChannel;
import com.eduplatform.notification.model.NotificationPriority;
import com.eduplatform.notification.model.Notification;
import com.eduplatform.notification.model.NotificationStatus;
import com.eduplatform.notification.model.NotificationType;
import com.eduplatform.notification.repository.NotificationRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InAppNotificationService {

    private final NotificationRepository notificationRepository;

    // Create In-App Notification
    @Transactional
    public Notification createInAppNotification(String userId, String title,
                                                String message, String description, String actionUrl) {
        Notification notification = new Notification();
        notification.setId(UUID.randomUUID().toString());
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setDescription(description);
        notification.setActionUrl(actionUrl);
        notification.setType(NotificationType.IN_APP);
        notification.setChannel(NotificationChannel.SYSTEM);
        notification.setStatus(NotificationStatus.DELIVERED);
        notification.setIsRead(false);
        notification.setPriority(NotificationPriority.MEDIUM);
        notification.setRetryCount(0);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setDeliveredAt(LocalDateTime.now());
        notification.setExpiresAt(LocalDateTime.now().plusDays(30));

        return notificationRepository.save(notification);
    }

    // Get Unread Notifications
    public List<Notification> getUnreadNotifications(String userId) {
        return notificationRepository.findByUserIdAndIsRead(userId, false);
    }

    // Get All Notifications
    public List<Notification> getAllNotifications(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // Mark as Read
    @Transactional
    public void markAsRead(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    // Mark All as Read
    @Transactional
    public void markAllAsRead(String userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndIsRead(userId, false);

        for (Notification notification : unread) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
        }

        notificationRepository.saveAll(unread);
    }

    // Delete Notification
    @Transactional
    public void deleteNotification(String notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    // Delete All Old Notifications
    @Scheduled(fixedDelay = 86400000) // Daily
    @Transactional
    public void deleteExpiredNotifications() {
        try {
            List<Notification> expired = notificationRepository.findAll()
                    .stream()
                    .filter(n -> n.getExpiresAt() != null && n.getExpiresAt().isBefore(LocalDateTime.now()))
                    .toList();

            notificationRepository.deleteAll(expired);
            log.info("Deleted {} expired notifications", expired.size());

        } catch (Exception e) {
            log.error("Error deleting expired notifications", e);
        }
    }

    // Get Unread Count
    public Long getUnreadCount(String userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }
}
