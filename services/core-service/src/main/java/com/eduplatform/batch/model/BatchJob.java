package com.eduplatform.batch.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "batch_jobs")
@CompoundIndex(name = "userId_tenantId_idx", def = "{'userId': 1, 'tenantId': 1}")
public class BatchJob {

    @Id
    private String id;

    @Indexed(unique = true)
    private String jobId; // JOB-XXXXX

    @Indexed
    private String jobType; // USER_IMPORT, COURSE_ASSIGNMENT, ENROLLMENT_BATCH, EXPORT

    @Indexed
    private String userId; // Submitted by

    private String fileName;

    private Long fileSizeBytes;

    @Indexed
    private String status; // QUEUED, PROCESSING, COMPLETED, FAILED, PAUSED

    private String sourceFormat; // CSV, EXCEL, JSON

    private Integer totalRecords;

    private Integer processedRecords;

    private Integer successRecords;

    private Integer failedRecords;

    private Integer skippedRecords;

    private Double progressPercentage;

    @Indexed
    private LocalDateTime createdAt;

    @Indexed
    private LocalDateTime startedAt;

    @Indexed
    private LocalDateTime completedAt;

    private Long executionTimeSeconds;

    private String errorSummary; // First few errors

    private Boolean isRetryable;

    private Integer retryCount;

    private String outputFileUrl; // For export jobs

    @Indexed
    private LocalDateTime expiresAt; // Auto-delete old jobs

    @Indexed
    private String tenantId;

    private Long version = 0L;

    /**
     * Is job still running
     */
    public boolean isRunning() {
        return "QUEUED".equals(status) || "PROCESSING".equals(status);
    }

    /**
     * Is job complete
     */
    public boolean isComplete() {
        return "COMPLETED".equals(status) || "FAILED".equals(status);
    }
}