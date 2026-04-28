package com.eduplatform.admin.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DisputeResolutionRequest {
    private String disputeId;
    private String resolution; // FULL_REFUND, PARTIAL_REFUND, REJECT
    private BigDecimal refundAmount;
    private String notes;
}