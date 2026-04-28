package com.eduplatform.batch.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "batch_audit_logs")
public class BatchAuditLog {

    @Id
    private String id;

    @Indexed
    private String jobId;

    @Indexed
    private String action; // CREATED, STARTED, PROCESSING, COMPLETED, FAILED, CANCELLED

    private String details; // What happened

    private String changedBy; // User ID

    private String previousStatus;

    private String newStatus;

    private Integer recordsProcessed;

    @Indexed
    private LocalDateTime timestamp;

    @Indexed
    private String tenantId;

    private Long version = 0L;
}