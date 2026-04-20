package com.eduplatform.notification.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Document(collection = "notifications")
public class Notification {
    @Id
    private String id;

    @Indexed
    private String userId;

    private String title;
    private String message;
    private String description;

    private NotificationType type; // EMAIL, SMS, PUSH, IN_APP
    private NotificationChannel channel;

    private String relatedEntityId; // courseId, lessonId, etc
    private String relatedEntityType; // COURSE, LESSON, PAYMENT, etc

    private String actionUrl;

    @Indexed
    private NotificationStatus status; // SENT, DELIVERED, FAILED, PENDING

    private String recipientEmail;
    private String recipientPhone;

    // For in-app notifications
    private Boolean isRead;
    private LocalDateTime readAt;

    // Tracking
    private Integer retryCount;
    private String errorMessage;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime expiresAt;

    // Email specific
    private String templateId;
    private Map<String, String> templateVariables;

    // Priority
    private NotificationPriority priority; // HIGH, MEDIUM, LOW

    @Indexed
    private String tenantId;
}
