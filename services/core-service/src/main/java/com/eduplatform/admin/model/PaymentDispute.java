package com.eduplatform.admin.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "payment_disputes")
public class PaymentDispute {
    @Id
    private String id;

    @Indexed
    private String paymentId;

    @Indexed
    private String userId;

    private String courseId;

    @Indexed
    private String instructorId;

    private BigDecimal amount;

    private DisputeReason reason;

    private String description;
    private List<String> evidenceUrls;

    @Indexed
    private DisputeStatus status;

    // Resolution
    private String resolvedBy;
    private String resolutionNotes;
    private DisputeResolution resolution; // REFUND, REJECT, PARTIAL

    private BigDecimal refundAmount;

    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    @Indexed
    private String tenantId;
}
