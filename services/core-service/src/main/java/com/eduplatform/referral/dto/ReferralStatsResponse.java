// FILE 21: ReferralStatsResponse.java
package com.eduplatform.referral.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ReferralStatsResponse {
    private String instructorId;
    private Long totalActiveCodes;
    private Long totalClicks;
    private Long totalConversions;
    private Double conversionRate;
    private Double totalEarned;
    private Double totalPending;
    private Double totalPaid;
}