// FILE 19: ReferralRewardResponse.java
package com.eduplatform.referral.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ReferralRewardResponse {
    private String rewardId;
    private String referralCode;
    private String courseName;
    private Double coursePrice;
    private Double rewardAmount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Boolean isExpired;
}