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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "coupon_codes")
@CompoundIndex(name = "code_tenantId_idx", def = "{'code': 1, 'tenantId': 1}", unique = true)
public class CouponCode {

    @Id
    private String id;

    @Indexed(unique = true)
    private String code; // Unique code

    @Indexed
    private String couponId; // Reference to Coupon

    @Indexed
    private String status; // ACTIVE, USED, EXPIRED

    private String redeemedBy; // User who redeemed (null if unused)

    @Indexed
    private LocalDateTime redeemedAt;

    @Indexed
    private LocalDateTime validUntil;

    @Indexed
    private LocalDateTime createdAt;

    @Indexed
    private String tenantId;

    private Long version = 0L;

    /**
     * Mark code as used
     */
    public void markAsUsed(String userId) {
        this.status = "USED";
        this.redeemedBy = userId;
        this.redeemedAt = LocalDateTime.now();
    }

    /**
     * Check if code is usable
     */
    public boolean isUsable() {
        if (!"ACTIVE".equals(status)) {
            return false;
        }

        if (validUntil != null && LocalDateTime.now().isAfter(validUntil)) {
            return false;
        }

        return true;
    }
}