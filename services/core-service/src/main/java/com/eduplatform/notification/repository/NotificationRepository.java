package com.eduplatform.notification.repository;

import com.eduplatform.notification.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByUserId(String userId);

    List<Notification> findByUserIdAndIsRead(String userId, Boolean isRead);

    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);

    List<Notification> findByStatusAndRetryCountLessThan(String status, Integer retryCount);

    List<Notification> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    Long countByUserIdAndIsReadFalse(String userId);
}