package com.eduplatform.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEventPayload {
    private String eventType;
    private String courseId;
    private String courseTitle;
    private String instructorId;
    private String instructorEmail;
    private String instructorName;
    private String targetEmail;
    private String notes;
    private String tenantId;
    private String timestamp;
}
