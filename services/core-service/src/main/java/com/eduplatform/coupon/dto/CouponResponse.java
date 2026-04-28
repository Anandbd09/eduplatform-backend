// FILE 18: CouponResponse.java
package com.eduplatform.coupon.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponResponse {
    private String id;
    private String code;
    private String name;
    private String description;
    private String discountType;
    private Double discountValue;
    private Double maxDiscount;
    private Double minPurchaseAmount;
    private Double maxRedemptions;
    private Long currentRedemptions;
    private Double maxRedemptionsPerUser;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private String status;
    private Boolean stackable;
    private LocalDateTime createdAt;
}