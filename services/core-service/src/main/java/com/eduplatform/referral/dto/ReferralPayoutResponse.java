// FILE 20: ReferralPayoutResponse.java
package com.eduplatform.referral.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ReferralPayoutResponse {
    private String payoutId;
    private String status;
    private Long rewardCount;
    private Double totalAmount;
    private Double platformFee;
    private Double netAmount;
    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime paidAt;
}