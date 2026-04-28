package com.eduplatform.reporting.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "appeal_requests")
public class AppealRequest {

    @Id
    private String id;

    @Indexed
    private String resolutionId;

    @Indexed
    private String disputeId;

    @Indexed
    private String appealerId;

    private String appealerName;

    private String reason; // Why they're appealing

    private String newEvidence; // Additional evidence

    @Indexed
    private String status; // PENDING, APPROVED, REJECTED

    private String reviewedBy;

    private String reviewReason;

    @Indexed
    private LocalDateTime createdAt;

    private LocalDateTime reviewedAt;

    @Indexed
    private String tenantId;

    private Long version = 0L;
}