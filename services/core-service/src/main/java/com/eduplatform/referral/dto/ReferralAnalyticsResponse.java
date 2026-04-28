// FILE 23: ReferralAnalyticsResponse.java
package com.eduplatform.referral.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ReferralAnalyticsResponse {
    private String referralCodeId;
    private Long totalClicks;
    private Long totalConversions;
    private Double conversionRate;
    private Double totalRevenueGenerated;
    private Double totalRewardsPaid;
    private Long uniqueVisitors;
    private String topCountry;
    private String topDeviceType;
}