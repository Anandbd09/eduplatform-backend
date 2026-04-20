package com.eduplatform.notification.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Document(collection = "notification_preferences")
public class NotificationPreference {
    @Id
    private String id;

    @Indexed
    private String userId;

    // Email preferences
    private Boolean emailEnabled;
    private Boolean emailCourseUpdates;
    private Boolean emailLiveSession;
    private Boolean emailPayment;
    private Boolean emailAchievements;
    private Boolean emailNewsletter;
    private Boolean emailWeeklyDigest;

    // SMS preferences
    private Boolean smsEnabled;
    private Boolean smsCourseUpdates;
    private Boolean smsLiveSession;
    private Boolean smsPayment;

    // Push notifications
    private Boolean pushEnabled;
    private Boolean pushCourseUpdates;
    private Boolean pushLiveSession;

    // Do not disturb
    private Boolean dndEnabled;
    private LocalTime dndStartTime;
    private LocalTime dndEndTime;

    // Frequency
    private NotificationFrequency digestFrequency; // DAILY, WEEKLY, MONTHLY

    // Unsubscribed channels
    private List<String> unsubscribedChannels;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Indexed
    private String tenantId;
}
