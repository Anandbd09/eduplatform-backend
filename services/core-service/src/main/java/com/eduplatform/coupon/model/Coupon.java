package com.eduplatform.coupon.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;
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
@Document(collection = "coupons")
@CompoundIndex(name = "code_tenantId_idx", def = "{'code': 1, 'tenantId': 1}", unique = true)
public class Coupon {

    @Id
    private String id;

    @Indexed(unique = true)
    private String code; // SUMMER2024, NEWYEAR50, etc.

    private String name; // "Summer Discount 2024"

    private String description;

    private String discountType; // PERCENTAGE, FIXED

    private Double discountValue; // 50 (for 50%) or 500 (for ₹500)

    private Double maxDiscount; // Max discount amount (for percentage)

    private Double minPurchaseAmount; // Min amount to apply coupon

    private Double maxRedemptions; // Total max redemptions (-1 = unlimited)

    @Indexed
    private Long currentRedemptions; // Current redemption count

    private Double maxRedemptionsPerUser; // Max times one user can use

    @Indexed
    private LocalDateTime validFrom;

    @Indexed
    private LocalDateTime validUntil;

    @Indexed
    private String status; // ACTIVE, INACTIVE, EXPIRED, ARCHIVED

    private List<String> applicableCourseIds; // Null = all courses

    private List<String> applicableUserRoles; // STUDENT, INSTRUCTOR

    private Boolean stackable; // Can be combined with other coupons

    @Indexed
    private LocalDateTime createdAt;

    @Indexed
    private LocalDateTime updatedAt;

    @Indexed
    private String createdBy; // Admin who created

    @Indexed
    private String tenantId;

    private Long version = 0L;

    /**
     * Check if coupon is valid for use
     */
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();

        if (!"ACTIVE".equals(status)) {
            return false;
        }

        if (now.isBefore(validFrom) || now.isAfter(validUntil)) {
            return false;
        }

        if (maxRedemptions != -1 && currentRedemptions >= maxRedemptions) {
            return false;
        }

        return true;
    }

    /**
     * Check if user can use coupon
     */
    public boolean canUserUse(Long userRedemptions) {
        if (!isValid()) {
            return false;
        }

        if (maxRedemptionsPerUser != -1 && userRedemptions >= maxRedemptionsPerUser) {
            return false;
        }

        return true;
    }
}