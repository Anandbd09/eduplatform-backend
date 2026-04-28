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
@Document(collection = "referral_codes")
@CompoundIndex(name = "instructorId_tenantId_idx", def = "{'instructorId': 1, 'tenantId': 1}")
public class ReferralCode {

    @Id
    private String id;

    @Indexed(unique = true)
    private String referralCode; // REF-XXXXX-XXXXX (12 chars)

    @Indexed
    private String instructorId;

    private String instructorName;

    private String instructorEmail;

    @Indexed
    private String status; // ACTIVE, INACTIVE, EXPIRED, ARCHIVED

    private String referralUrl; // https://eduplatform.com?ref=REF-XXXXX

    private Long totalClicks;

    private Long totalConversions; // Purchases from this referral

    private Double totalRewardEarned; // Total INR earned

    private Double totalRewardPending; // Not yet paid out

    private Double totalRewardPaid; // Already paid to instructor

    @Indexed
    private LocalDateTime createdAt;

    @Indexed
    private LocalDateTime expiresAt; // Optional expiry

    private Long clicksLastMonth;

    private Long conversionsLastMonth;

    private Double revenueLastMonth;

    @Indexed
    private String tenantId;

    private Long version = 0L;

    /**
     * Is referral code active and not expired
     */
    public boolean isActive() {
        if ("INACTIVE".equals(status) || "ARCHIVED".equals(status)) {
            return false;
        }

        if ("EXPIRED".equals(status)) {
            return false;
        }

        if (expiresAt != null && LocalDateTime.now().isAfter(expiresAt)) {
            return false;
        }

        return "ACTIVE".equals(status);
    }

    /**
     * Get conversion rate
     */
    public double getConversionRate() {
        if (totalClicks == null || totalClicks == 0) {
            return 0.0;
        }
        return ((double) (totalConversions != null ? totalConversions : 0) / totalClicks) * 100;
    }
}