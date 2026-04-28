package com.eduplatform.export.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "export_jobs")
public class ExportJob {

    @Id
    private String id;

    @Indexed(unique = true)
    private String jobId; // EXP-XXXXX format

    @Indexed
    private String userId;

    @Indexed
    private String exportType; // USER_DATA, COURSE_DATA, PAYMENT_DATA, GDPR_DATA

    @Indexed
    private String format; // CSV, PDF, EXCEL, JSON

    private String sourceEntity; // Users, Courses, Orders, etc

    @Indexed
    private String status; // QUEUED, PROCESSING, COMPLETED, FAILED, ARCHIVED

    private String filterCriteria; // JSON filter

    private Integer totalRecords;

    private Integer exportedRecords;

    private Integer failedRecords;

    @Indexed
    private LocalDateTime createdAt;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private Long fileSize; // Bytes

    private String filePath; // Storage location

    private String downloadUrl; // Public download URL

    @Indexed
    private LocalDateTime expiresAt; // Auto-delete after 30 days

    private String errorMessage;

    private Long processingTimeMs;

    private Boolean isEncrypted; // GDPR compliance

    private String encryptionKey; // For GDPR data

    private String notificationEmail;

    @Indexed
    private String tenantId;

    private Long version_field = 0L;

    /**
     * Is export still valid (not expired)
     */
    public boolean isStillValid() {
        if (expiresAt == null) return true;
        return LocalDateTime.now().isBefore(expiresAt);
    }

    /**
     * Is export processing
     */
    public boolean isProcessing() {
        return "PROCESSING".equals(status);
    }
}