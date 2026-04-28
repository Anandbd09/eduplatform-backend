package com.eduplatform.coupon.service;

import com.eduplatform.coupon.model.*;
import com.eduplatform.coupon.repository.*;
import com.eduplatform.coupon.dto.RedemptionResponse;
import com.eduplatform.coupon.exception.CouponException;
import com.eduplatform.coupon.util.DiscountCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class RedemptionService {

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponRedemptionRepository redemptionRepository;

    @Autowired
    private CouponAnalyticsRepository analyticsRepository;

    /**
     * REDEEM COUPON - MAIN LOGIC
     */
    public RedemptionResponse redeemCoupon(Coupon coupon, String userId, String orderId,
                                           Double originalAmount, String tenantId) {
        try {
            // Validate coupon
            if (!coupon.isValid()) {
                throw new CouponException("Coupon is not valid");
            }

            // Check minimum purchase
            if (originalAmount < coupon.getMinPurchaseAmount()) {
                throw new CouponException("Minimum purchase amount of " +
                        coupon.getMinPurchaseAmount() + " required");
            }

            // Check user limit
            Long userRedemptions = redemptionRepository
                    .countByUserIdAndCouponIdAndTenantId(userId, coupon.getId(), tenantId);
            if (!coupon.canUserUse(userRedemptions)) {
                throw new CouponException("Maximum usage limit reached for this user");
            }

            // Calculate discount
            Double discountAmount = DiscountCalculator.calculateDiscount(
                    coupon.getDiscountType(),
                    originalAmount,
                    coupon.getDiscountValue(),
                    coupon.getMaxDiscount()
            );

            Double finalAmount = originalAmount - discountAmount;

            // Create redemption record
            CouponRedemption redemption = CouponRedemption.builder()
                    .id(UUID.randomUUID().toString())
                    .userId(userId)
                    .couponId(coupon.getId())
                    .code(coupon.getCode())
                    .orderId(orderId)
                    .originalAmount(originalAmount)
                    .discountAmount(discountAmount)
                    .finalAmount(finalAmount)
                    .discountType(coupon.getDiscountType())
                    .status("SUCCESS")
                    .redeemedAt(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .tenantId(tenantId)
                    .build();

            CouponRedemption saved = redemptionRepository.save(redemption);

            // Update coupon redemption count
            coupon.setCurrentRedemptions(coupon.getCurrentRedemptions() + 1);
            couponRepository.save(coupon);

            // Update analytics
            updateAnalytics(coupon.getId(), discountAmount, finalAmount, tenantId);

            log.info("Coupon redeemed: code={}, userId={}, discount={}",
                    coupon.getCode(), userId, discountAmount);

            return RedemptionResponse.builder()
                    .id(saved.getId())
                    .code(saved.getCode())
                    .orderId(saved.getOrderId())
                    .originalAmount(saved.getOriginalAmount())
                    .discountAmount(saved.getDiscountAmount())
                    .finalAmount(saved.getFinalAmount())
                    .status(saved.getStatus())
                    .redeemedAt(saved.getRedeemedAt())
                    .build();

        } catch (CouponException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error redeeming coupon", e);
            throw new CouponException("Failed to redeem coupon: " + e.getMessage());
        }
    }

    /**
     * REVERSE REDEMPTION
     */
    public void reverseRedemption(String redemptionId, String tenantId) {
        try {
            var redemption = redemptionRepository.findByIdAndTenantId(redemptionId, tenantId)
                    .orElseThrow(() -> new CouponException("Redemption not found"));

            if ("REVERSED".equals(redemption.getStatus())) {
                throw new CouponException("Redemption already reversed");
            }

            // Update redemption
            redemption.setStatus("REVERSED");
            redemption.setReversedAt(LocalDateTime.now());
            redemptionRepository.save(redemption);

            // Update coupon count
            var coupon = couponRepository.findById(redemption.getCouponId())
                    .orElseThrow(() -> new CouponException("Coupon not found"));
            coupon.setCurrentRedemptions(Math.max(0, coupon.getCurrentRedemptions() - 1));
            couponRepository.save(coupon);

            log.info("Redemption reversed: id={}", redemptionId);

        } catch (Exception e) {
            log.error("Error reversing redemption", e);
            throw new CouponException("Failed to reverse redemption");
        }
    }

    /**
     * UPDATE ANALYTICS AFTER REDEMPTION
     */
    private void updateAnalytics(String couponId, Double discountAmount,
                                 Double finalAmount, String tenantId) {
        try {
            var analytics = analyticsRepository.findByCouponIdAndTenantId(couponId, tenantId)
                    .orElse(null);

            if (analytics != null) {
                analytics.setTotalRedemptions(analytics.getTotalRedemptions() + 1);
                analytics.setTotalDiscountGiven(analytics.getTotalDiscountGiven() + discountAmount);
                analytics.setTotalRevenue(analytics.getTotalRevenue() + finalAmount);
                analytics.setLastRedemption(LocalDateTime.now());
                analytics.setUpdatedAt(LocalDateTime.now());
                analyticsRepository.save(analytics);
            }
        } catch (Exception e) {
            log.error("Error updating analytics", e);
        }
    }
}