package com.eduplatform.coupon.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "promotion_campaigns")
public class PromotionCampaign {

    @Id
    private String id;

    @Indexed(unique = true)
    private String campaignCode;

    private String name;

    private String description;

    private String type; // SEASONAL, FLASH_SALE, REFERRAL, NEW_YEAR

    @Indexed
    private LocalDateTime startDate;

    @Indexed
    private LocalDateTime endDate;

    @Indexed
    private String status; // PLANNED, ACTIVE, INACTIVE, ENDED

    private List<String> couponIds; // Coupons in this campaign

    private String targetAudience; // ALL, NEW_USERS, EXISTING_USERS, SPECIFIC_COURSES

    private Long expectedReach; // Expected users to reach

    private Long actualReach; // Actual users reached

    private Double budgetAllocated;

    private Double budgetUsed;

    private String promotionChannel; // EMAIL, WHATSAPP, IN_APP, SOCIAL

    @Indexed
    private LocalDateTime createdAt;

    @Indexed
    private String createdBy;

    @Indexed
    private String tenantId;

    private Long version = 0L;

    /**
     * Check if campaign is active
     */
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return "ACTIVE".equals(status) && now.isAfter(startDate) && now.isBefore(endDate);
    }
}