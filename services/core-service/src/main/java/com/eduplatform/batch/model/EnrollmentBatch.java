package com.eduplatform.batch.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "enrollment_batches")
public class EnrollmentBatch {

    @Id
    private String id;

    @Indexed
    private String jobId;

    private List<String> userIds;

    private List<String> courseIds;

    private Integer totalEnrollments;

    private Integer successfulEnrollments;

    private Integer duplicateEnrollments;

    private Integer failedEnrollments;

    @Indexed
    private String status; // QUEUED, PROCESSING, COMPLETED, FAILED

    private Boolean overwriteExisting; // Overwrite if already enrolled

    private Boolean sendWelcomeEmail;

    @Indexed
    private LocalDateTime createdAt;

    @Indexed
    private LocalDateTime completedAt;

    @Indexed
    private String tenantId;

    private Long version = 0L;
}