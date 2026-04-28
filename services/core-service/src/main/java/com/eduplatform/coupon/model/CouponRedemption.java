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
@Document(collection = "coupon_redemptions")
@CompoundIndex(name = "userId_couponId_idx", def = "{'userId': 1, 'couponId': 1, 'tenantId': 1}")
public class CouponRedemption {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String couponId;

    private String code;

    private String orderId; // Order/transaction ID

    private Double originalAmount; // Amount before discount

    private Double discountAmount; // Amount discounted

    private Double finalAmount; // Amount after discount

    private String discountType; // PERCENTAGE, FIXED

    @Indexed
    private String status; // SUCCESS, FAILED, REVERSED

    private String failureReason;

    @Indexed
    private LocalDateTime redeemedAt;

    private LocalDateTime reversedAt;

    @Indexed
    private LocalDateTime createdAt;

    @Indexed
    private String tenantId;

    private Long version = 0L;
}