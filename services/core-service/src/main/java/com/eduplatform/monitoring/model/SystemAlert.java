package com.eduplatform.monitoring.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "system_alerts")
public class SystemAlert {

    @Id
    private String id;

    @Indexed
    private String alertType; // PERFORMANCE, ERROR, MEMORY, DATABASE, SECURITY

    @Indexed
    private String severity; // INFO, WARNING, CRITICAL

    private String title;

    private String description;

    private String condition; // What triggered this

    private Double threshold;

    private Double currentValue;

    @Indexed
    private String status; // ACTIVE, ACKNOWLEDGED, RESOLVED

    @Indexed
    private LocalDateTime createdAt;

    private LocalDateTime acknowledgedAt;

    private String acknowledgedBy;

    private LocalDateTime resolvedAt;

    private String resolvedBy;

    private String resolutionNotes;

    private Boolean notificationSent;

    private String notificationChannel; // EMAIL, SMS, SLACK

    @Indexed
    private String tenantId;

    private Long version_field = 0L;

    /**
     * Is alert still active
     */
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }
}