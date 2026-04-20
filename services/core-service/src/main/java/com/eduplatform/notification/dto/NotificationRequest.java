package com.eduplatform.notification.dto;

import lombok.Data;
import java.util.Map;

@Data
public class NotificationRequest {
    private String userId;
    private String title;
    private String message;
    private String description;
    private String templateId; // For email
    private Map<String, String> templateVariables;
    private String relatedEntityId;
    private String relatedEntityType;
    private String actionUrl;
    private String channel; // COURSE_ENROLLMENT, PAYMENT, etc
    private String priority; // HIGH, MEDIUM, LOW
    private Boolean sendEmail;
    private Boolean sendSMS;
    private Boolean sendPush;
    private Boolean sendInApp;
}