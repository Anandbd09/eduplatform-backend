// FILE 17: CouponRequest.java
package com.eduplatform.coupon.dto;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponRequest {
    private String code;
    private String name;
    private String description;
    private String discountType; // PERCENTAGE, FIXED
    private Double discountValue;
    private Double maxDiscount;
    private Double minPurchaseAmount;
    private Double maxRedemptions;
    private Double maxRedemptionsPerUser;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private String status;
    private List<String> applicableCourseIds;
    private List<String> applicableUserRoles;
    private Boolean stackable;
}