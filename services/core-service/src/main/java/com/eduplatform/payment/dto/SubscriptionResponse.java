package com.eduplatform.payment.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SubscriptionResponse {
    private String subscriptionId;
    private String planId;
    private String planName;
    private BigDecimal planAmount;
    private String status;
    private LocalDateTime currentCycleEnd;
    private LocalDateTime nextBillingDate;
    private Boolean autoRenewal;
    private Integer cycleCount;
    private List<String> features;
}