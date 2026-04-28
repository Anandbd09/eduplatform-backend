package com.eduplatform.coupon.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "coupon_analytics")
public class CouponAnalytics {

    @Id
    private String id;

    @Indexed(unique = true)
    private String couponId;

    private Long totalRedemptions;

    private Long totalUsages;

    private Double totalDiscountGiven;

    private Double totalRevenue; // Revenue after applying discount

    private Double conversionRate; // (Redemptions / Impressions) * 100

    private Long impressions; // Times shown to users

    private Long clicks; // Times coupon code clicked

    private Double avgDiscountPerRedemption;

    private Long uniqueUsers; // Unique users who redeemed

    @Indexed
    private LocalDateTime lastRedemption;

    @Indexed
    private LocalDateTime updatedAt;

    @Indexed
    private String tenantId;

    private Long version = 0L;
}