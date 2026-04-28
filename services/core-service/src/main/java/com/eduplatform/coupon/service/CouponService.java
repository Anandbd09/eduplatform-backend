package com.eduplatform.coupon.service;

import com.eduplatform.coupon.model.*;
import com.eduplatform.coupon.repository.*;
import com.eduplatform.coupon.dto.*;
import com.eduplatform.coupon.exception.CouponException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class CouponService {

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponCodeRepository couponCodeRepository;

    @Autowired
    private CouponRedemptionRepository redemptionRepository;

    @Autowired
    private CouponAnalyticsRepository analyticsRepository;

    @Autowired
    private RedemptionService redemptionService;

    @Autowired
    private CouponValidationService validationService;

    @Autowired
    private AnalyticsService analyticsService;

    /**
     * CREATE NEW COUPON (ADMIN ONLY)
     */
    public CouponResponse createCoupon(CouponRequest request, String tenantId, String adminId) {
        try {
            // Validate request
            if (request.getValidFrom().isAfter(request.getValidUntil())) {
                throw new CouponException("Valid from date must be before valid until date");
            }

            // Check if code already exists
            Optional<Coupon> existing = couponRepository.findByCodeAndTenantId(request.getCode(), tenantId);
            if (existing.isPresent()) {
                throw new CouponException("Coupon code already exists: " + request.getCode());
            }

            // Create coupon
            Coupon coupon = Coupon.builder()
                    .id(UUID.randomUUID().toString())
                    .code(request.getCode().toUpperCase())
                    .name(request.getName())
                    .description(request.getDescription())
                    .discountType(request.getDiscountType()) // PERCENTAGE, FIXED
                    .discountValue(request.getDiscountValue())
                    .maxDiscount(request.getMaxDiscount())
                    .minPurchaseAmount(request.getMinPurchaseAmount())
                    .maxRedemptions(request.getMaxRedemptions() != null ? request.getMaxRedemptions() : -1)
                    .currentRedemptions(0L)
                    .maxRedemptionsPerUser(request.getMaxRedemptionsPerUser() != null ? request.getMaxRedemptionsPerUser() : -1)
                    .validFrom(request.getValidFrom())
                    .validUntil(request.getValidUntil())
                    .status("ACTIVE")
                    .applicableCourseIds(request.getApplicableCourseIds())
                    .applicableUserRoles(request.getApplicableUserRoles())
                    .stackable(request.getStackable() != null ? request.getStackable() : false)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .createdBy(adminId)
                    .tenantId(tenantId)
                    .build();

            Coupon saved = couponRepository.save(coupon);

            // Create analytics record
            CouponAnalytics analytics = CouponAnalytics.builder()
                    .id(UUID.randomUUID().toString())
                    .couponId(saved.getId())
                    .totalRedemptions(0L)
                    .totalUsages(0L)
                    .totalDiscountGiven(0.0)
                    .totalRevenue(0.0)
                    .impressions(0L)
                    .clicks(0L)
                    .uniqueUsers(0L)
                    .updatedAt(LocalDateTime.now())
                    .tenantId(tenantId)
                    .build();
            analyticsRepository.save(analytics);

            log.info("Coupon created: code={}, by={}, tenant={}", saved.getCode(), adminId, tenantId);
            return convertToResponse(saved);

        } catch (CouponException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating coupon", e);
            throw new CouponException("Failed to create coupon: " + e.getMessage());
        }
    }

    /**
     * VALIDATE COUPON
     */
    public ValidationResponse validateCoupon(String code, String userId, Double amount, String tenantId) {
        try {
            Optional<Coupon> couponOpt = couponRepository.findByCodeAndTenantId(code.toUpperCase(), tenantId);

            if (couponOpt.isEmpty()) {
                return ValidationResponse.builder()
                        .valid(false)
                        .message("Coupon not found")
                        .build();
            }

            Coupon coupon = couponOpt.get();

            // Use validation service
            return validationService.validateCoupon(coupon, userId, amount, tenantId);

        } catch (Exception e) {
            log.error("Error validating coupon", e);
            return ValidationResponse.builder()
                    .valid(false)
                    .message("Validation failed: " + e.getMessage())
                    .build();
        }
    }

    /**
     * REDEEM COUPON
     */
    public RedemptionResponse redeemCoupon(RedemptionRequest request, String userId, String tenantId) {
        try {
            Optional<Coupon> couponOpt = couponRepository.findByCodeAndTenantId(request.getCode().toUpperCase(), tenantId);

            if (couponOpt.isEmpty()) {
                throw new CouponException("Coupon not found");
            }

            Coupon coupon = couponOpt.get();

            // Use redemption service
            return redemptionService.redeemCoupon(coupon, userId, request.getOrderId(),
                    request.getOriginalAmount(), tenantId);

        } catch (CouponException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error redeeming coupon", e);
            throw new CouponException("Failed to redeem coupon: " + e.getMessage());
        }
    }

    /**
     * GET ACTIVE COUPONS
     */
    public List<CouponResponse> getActiveCoupons(String tenantId) {
        try {
            List<Coupon> coupons = couponRepository.findActiveCoupons(tenantId);
            return coupons.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching active coupons", e);
            throw new CouponException("Failed to fetch active coupons");
        }
    }

    /**
     * GET COUPON BY CODE
     */
    public CouponResponse getCouponByCode(String code, String tenantId) {
        try {
            Optional<Coupon> coupon = couponRepository.findByCodeAndTenantId(code.toUpperCase(), tenantId);
            return coupon.map(this::convertToResponse)
                    .orElseThrow(() -> new CouponException("Coupon not found"));
        } catch (Exception e) {
            log.error("Error fetching coupon", e);
            throw new CouponException("Failed to fetch coupon");
        }
    }

    /**
     * GET COUPON BY ID
     */
    public CouponResponse getCouponById(String id, String tenantId) {
        try {
            Optional<Coupon> coupon = couponRepository.findById(id);
            if (coupon.isPresent() && coupon.get().getTenantId().equals(tenantId)) {
                return convertToResponse(coupon.get());
            }
            throw new CouponException("Coupon not found");
        } catch (Exception e) {
            log.error("Error fetching coupon", e);
            throw new CouponException("Failed to fetch coupon");
        }
    }

    /**
     * GET ALL COUPONS (PAGINATED)
     */
    public Page<CouponResponse> getAllCoupons(int page, int size, String status, String tenantId) {
        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

            Page<Coupon> coupons;
            if (status != null && !status.isEmpty()) {
                coupons = couponRepository.findByStatusAndTenantId(status, tenantId, pageable);
            } else {
                coupons = couponRepository.findAll(pageable);
            }

            return coupons.map(this::convertToResponse);

        } catch (Exception e) {
            log.error("Error fetching coupons", e);
            throw new CouponException("Failed to fetch coupons");
        }
    }

    /**
     * UPDATE COUPON
     */
    public CouponResponse updateCoupon(String id, CouponRequest request, String tenantId) {
        try {
            Optional<Coupon> couponOpt = couponRepository.findById(id);
            if (couponOpt.isEmpty()) {
                throw new CouponException("Coupon not found");
            }

            Coupon coupon = couponOpt.get();
            if (!coupon.getTenantId().equals(tenantId)) {
                throw new CouponException("Unauthorized");
            }

            coupon.setName(request.getName());
            coupon.setDescription(request.getDescription());
            coupon.setDiscountType(request.getDiscountType());
            coupon.setDiscountValue(request.getDiscountValue());
            coupon.setMaxDiscount(request.getMaxDiscount());
            coupon.setMinPurchaseAmount(request.getMinPurchaseAmount());
            coupon.setStatus(request.getStatus());
            coupon.setApplicableCourseIds(request.getApplicableCourseIds());
            coupon.setApplicableUserRoles(request.getApplicableUserRoles());
            coupon.setUpdatedAt(LocalDateTime.now());

            Coupon updated = couponRepository.save(coupon);
            log.info("Coupon updated: id={}", id);
            return convertToResponse(updated);

        } catch (CouponException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating coupon", e);
            throw new CouponException("Failed to update coupon");
        }
    }

    /**
     * DELETE COUPON
     */
    public void deleteCoupon(String id, String tenantId) {
        try {
            Optional<Coupon> couponOpt = couponRepository.findById(id);
            if (couponOpt.isEmpty()) {
                throw new CouponException("Coupon not found");
            }

            Coupon coupon = couponOpt.get();
            if (!coupon.getTenantId().equals(tenantId)) {
                throw new CouponException("Unauthorized");
            }

            coupon.setStatus("ARCHIVED");
            coupon.setUpdatedAt(LocalDateTime.now());
            couponRepository.save(coupon);

            log.info("Coupon archived: id={}", id);

        } catch (CouponException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting coupon", e);
            throw new CouponException("Failed to delete coupon");
        }
    }

    /**
     * GET USER REDEMPTION HISTORY
     */
    public Page<RedemptionResponse> getUserRedemptions(String userId, int page, int size, String tenantId) {
        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);
            Pageable pageable = PageRequest.of(page, size, Sort.by("redeemedAt").descending());

            Page<CouponRedemption> redemptions = redemptionRepository
                    .findByUserIdAndTenantId(userId, tenantId, pageable);

            return redemptions.map(this::convertRedemptionToResponse);

        } catch (Exception e) {
            log.error("Error fetching user redemptions", e);
            throw new CouponException("Failed to fetch redemptions");
        }
    }

    /**
     * GET COUPON ANALYTICS
     */
    public CouponAnalyticsResponse getCouponAnalytics(String couponId, String tenantId) {
        try {
            Optional<CouponAnalytics> analytics = analyticsRepository.findByCouponIdAndTenantId(couponId, tenantId);
            return analytics.map(a -> CouponAnalyticsResponse.builder()
                            .couponId(a.getCouponId())
                            .totalRedemptions(a.getTotalRedemptions())
                            .totalDiscountGiven(a.getTotalDiscountGiven())
                            .totalRevenue(a.getTotalRevenue())
                            .conversionRate(a.getConversionRate())
                            .uniqueUsers(a.getUniqueUsers())
                            .avgDiscountPerRedemption(a.getAvgDiscountPerRedemption())
                            .lastRedemption(a.getLastRedemption())
                            .build())
                    .orElseThrow(() -> new CouponException("Analytics not found"));
        } catch (Exception e) {
            log.error("Error fetching analytics", e);
            throw new CouponException("Failed to fetch analytics");
        }
    }

    /**
     * CONVERT COUPON TO RESPONSE
     */
    private CouponResponse convertToResponse(Coupon coupon) {
        return CouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .name(coupon.getName())
                .description(coupon.getDescription())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .maxDiscount(coupon.getMaxDiscount())
                .minPurchaseAmount(coupon.getMinPurchaseAmount())
                .maxRedemptions(coupon.getMaxRedemptions())
                .currentRedemptions(coupon.getCurrentRedemptions())
                .maxRedemptionsPerUser(coupon.getMaxRedemptionsPerUser())
                .validFrom(coupon.getValidFrom())
                .validUntil(coupon.getValidUntil())
                .status(coupon.getStatus())
                .stackable(coupon.getStackable())
                .createdAt(coupon.getCreatedAt())
                .build();
    }

    /**
     * CONVERT REDEMPTION TO RESPONSE
     */
    private RedemptionResponse convertRedemptionToResponse(CouponRedemption redemption) {
        return RedemptionResponse.builder()
                .id(redemption.getId())
                .code(redemption.getCode())
                .orderId(redemption.getOrderId())
                .originalAmount(redemption.getOriginalAmount())
                .discountAmount(redemption.getDiscountAmount())
                .finalAmount(redemption.getFinalAmount())
                .status(redemption.getStatus())
                .redeemedAt(redemption.getRedeemedAt())
                .build();
    }
}