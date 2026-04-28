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
@Document(collection = "batch_job_results")
public class BatchJobResult {

    @Id
    private String id;

    @Indexed
    private String jobId;

    @Indexed
    private Integer recordNumber;

    private String recordIdentifier; // email, userId, etc

    private String status; // SUCCESS, FAILED, SKIPPED

    private String message; // Success or error message

    private String errorCode; // VALIDATION_ERROR, DUPLICATE, etc

    private String errorDetails; // Detailed error info

    private Object originalData;

    private Object processedData;

    @Indexed
    private LocalDateTime createdAt;

    @Indexed
    private String tenantId;

    private Long version = 0L;
}