package com.eduplatform.notification.repository;

import com.eduplatform.notification.model.NotificationLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationLogRepository extends MongoRepository<NotificationLog, String> {
    List<NotificationLog> findByNotificationId(String notificationId);

    List<NotificationLog> findByUserIdOrderByCreatedAtDesc(String userId);

    List<NotificationLog> findByStatus(String status);

    List<NotificationLog> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);
}