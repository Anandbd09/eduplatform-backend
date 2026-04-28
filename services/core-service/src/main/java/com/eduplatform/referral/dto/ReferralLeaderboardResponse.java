// FILE 22: ReferralLeaderboardResponse.java
package com.eduplatform.referral.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ReferralLeaderboardResponse {
    private Integer rank;
    private String instructorId;
    private String instructorName;
    private Long totalClicks;
    private Long totalConversions;
    private Double totalEarned;
    private Double conversionRate;
}