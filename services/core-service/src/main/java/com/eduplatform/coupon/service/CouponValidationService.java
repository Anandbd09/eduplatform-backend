package com.eduplatform.coupon.service;

import com.eduplatform.coupon.model.Coupon;
import com.eduplatform.coupon.dto.ValidationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional(readOnly = true)
public class CouponValidationService {

    /**
     * VALIDATE COUPON AGAINST RULES
     */
    public ValidationResponse validateCoupon(Coupon coupon, String userId,
                                             Double amount, String tenantId) {
        try {
            // Check status
            if (!"ACTIVE".equals(coupon.getStatus())) {
                return ValidationResponse.builder()
                        .valid(false)
                        .message("Coupon is not active")
                        .build();
            }

            // Check dates
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(coupon.getValidFrom())) {
                return ValidationResponse.builder()
                        .valid(false)
                        .message("Coupon is not yet valid")
                        .build();
            }

            if (now.isAfter(coupon.getValidUntil())) {
                return ValidationResponse.builder()
                        .valid(false)
                        .message("Coupon has expired")
                        .build();
            }

            // Check redemption limit
            if (coupon.getMaxRedemptions() != -1 &&
                    coupon.getCurrentRedemptions() >= coupon.getMaxRedemptions()) {
                return ValidationResponse.builder()
                        .valid(false)
                        .message("Coupon redemption limit reached")
                        .build();
            }

            // Check minimum purchase
            if (amount < coupon.getMinPurchaseAmount()) {
                return ValidationResponse.builder()
                        .valid(false)
                        .message("Minimum purchase amount of " + coupon.getMinPurchaseAmount() + " required")
                        .discountAmount(0.0)
                        .finalAmount(amount)
                        .build();
            }

            return ValidationResponse.builder()
                    .valid(true)
                    .message("Coupon is valid")
                    .discountValue(coupon.getDiscountValue())
                    .discountType(coupon.getDiscountType())
                    .maxDiscount(coupon.getMaxDiscount())
                    .build();

        } catch (Exception e) {
            log.error("Error validating coupon", e);
            return ValidationResponse.builder()
                    .valid(false)
                    .message("Validation error: " + e.getMessage())
                    .build();
        }
    }
}