package com.eduplatform.referral.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "referral_rewards")
@CompoundIndex(name = "instructorId_expiresAt_idx", def = "{'instructorId': 1, 'expiresAt': 1}")
public class ReferralReward {

    @Id
    private String id;

    @Indexed
    private String instructorId;

    @Indexed
    private String referralCode;

    @Indexed
    private String referralClickId; // Which click generated this reward

    private String orderId; // Order that generated reward

    private String courseId;

    private String courseName;

    private Double coursePrice; // Original price

    private Double rewardAmount; // 20% of course price (platform cut)

    @Indexed
    private String status; // PENDING, APPROVED, EXPIRED, PAID

    @Indexed
    private LocalDateTime createdAt;

    @Indexed
    private LocalDateTime expiresAt; // 30 days from click (auto-expire if not purchased)

    private LocalDateTime approvedAt;

    private LocalDateTime paidAt;

    @Indexed
    private LocalDateTime purchasedAt; // When referral converted

    private String referrerName; // Name of person referred (for tracking)

    private String referrerEmail;

    @Indexed
    private String tenantId;

    private Long version = 0L;

    /**
     * Check if reward is expired (30 days from click)
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}