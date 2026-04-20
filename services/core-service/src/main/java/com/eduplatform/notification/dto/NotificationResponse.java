package com.eduplatform.notification.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationResponse {
    private String notificationId;
    private String title;
    private String message;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private Boolean isRead;
}