package com.eduplatform.notification.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Document(collection = "notification_logs")
public class NotificationLog {
    @Id
    private String id;

    @Indexed
    private String notificationId;

    @Indexed
    private String userId;

    private NotificationType type;

    private String recipient;

    private String subject;
    private String message;

    @Indexed
    private NotificationStatus status;

    private String externalServiceId; // Resend message ID, Twilio SID, etc

    private Integer attempts;
    private String lastErrorMessage;

    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;

    @Indexed
    private LocalDateTime createdAt;
}