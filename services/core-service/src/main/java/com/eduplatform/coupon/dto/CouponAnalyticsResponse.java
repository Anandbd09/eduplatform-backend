// FILE 22: CouponAnalyticsResponse.java
package com.eduplatform.coupon.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponAnalyticsResponse {
    private String couponId;
    private Long totalRedemptions;
    private Double totalDiscountGiven;
    private Double totalRevenue;
    private Double conversionRate;
    private Long uniqueUsers;
    private Double avgDiscountPerRedemption;
    private LocalDateTime lastRedemption;
}