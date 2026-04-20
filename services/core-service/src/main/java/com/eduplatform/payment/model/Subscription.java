package com.eduplatform.payment.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "subscriptions")
@CompoundIndex(name = "userId_active_idx", def = "{'userId': 1, 'isActive': 1}")
public class Subscription {
    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String planId;

    @Indexed
    private String razorpaySubscriptionId;

    private SubscriptionPlan plan; // MONTHLY, QUARTERLY, ANNUAL

    private BigDecimal planAmount;
    private String currency;

    @Indexed
    private SubscriptionStatus status; // ACTIVE, PAUSED, CANCELLED, EXPIRED

    private Integer cycleCount;
    private LocalDateTime currentCycleStart;
    private LocalDateTime currentCycleEnd;
    private LocalDateTime nextBillingDate;

    private Boolean autoRenewal;
    private Boolean isActive;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime pausedAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;

    // Payment history
    private Integer totalPayments;
    private LocalDateTime lastPaymentDate;
    private BigDecimal totalPaid;

    // Razorpay Details
    private String shortUrl;
    private String notes;

    // Features
    private List<String> includedFeatures;

    @Indexed
    private String tenantId;
}
