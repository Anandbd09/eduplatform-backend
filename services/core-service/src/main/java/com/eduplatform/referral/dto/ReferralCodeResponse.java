// FILE 18: ReferralCodeResponse.java
package com.eduplatform.referral.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ReferralCodeResponse {
    private String referralCode;
    private String referralUrl;
    private String status;
    private Long totalClicks;
    private Long totalConversions;
    private Double conversionRate;
    private Double totalRewardEarned;
    private Double totalRewardPending;
    private Double totalRewardPaid;
    private LocalDateTime createdAt;
}