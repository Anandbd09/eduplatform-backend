package com.eduplatform.coupon.controller;

import com.eduplatform.coupon.service.CouponService;
import com.eduplatform.coupon.dto.*;
import com.eduplatform.coupon.exception.CouponException;
import com.eduplatform.common.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/coupons")
public class CouponController {

    @Autowired
    private CouponService couponService;

    /**
     * ENDPOINT 1: Get active coupons
     * GET /api/v1/coupons/active
     */
    @GetMapping("/active")
    public ResponseEntity<?> getActiveCoupons(
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            var coupons = couponService.getActiveCoupons(tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Active coupons retrieved", coupons));
        } catch (Exception e) {
            log.error("Error fetching active coupons", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch coupons", null));
        }
    }

    /**
     * ENDPOINT 2: Validate coupon
     * GET /api/v1/coupons/validate?code=SUMMER2024&amount=5000
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateCoupon(
            @RequestParam String code,
            @RequestParam Double amount,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            ValidationResponse response = couponService.validateCoupon(code, userId, amount, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Validation completed", response));
        } catch (Exception e) {
            log.error("Error validating coupon", e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Validation failed", null));
        }
    }

    /**
     * ENDPOINT 3: Redeem coupon
     * POST /api/v1/coupons/redeem
     */
    @PostMapping("/redeem")
    public ResponseEntity<?> redeemCoupon(
            @RequestBody RedemptionRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            RedemptionResponse response = couponService.redeemCoupon(request, userId, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Coupon redeemed successfully", response));
        } catch (CouponException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error redeeming coupon", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to redeem coupon", null));
        }
    }

    /**
     * ENDPOINT 4: Get coupon by code
     * GET /api/v1/coupons/{code}
     */
    @GetMapping("/{code}")
    public ResponseEntity<?> getCouponByCode(
            @PathVariable String code,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            CouponResponse coupon = couponService.getCouponByCode(code, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Coupon retrieved", coupon));
        } catch (CouponException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error fetching coupon", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch coupon", null));
        }
    }

    /**
     * ENDPOINT 5: Get user redemption history
     * GET /api/v1/coupons/my-redemptions?page=0&size=10
     */
    @GetMapping("/my-redemptions")
    public ResponseEntity<?> getMyRedemptions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            Page<RedemptionResponse> redemptions = couponService.getUserRedemptions(userId, page, size, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Redemption history retrieved", redemptions));
        } catch (Exception e) {
            log.error("Error fetching redemption history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch history", null));
        }
    }
}