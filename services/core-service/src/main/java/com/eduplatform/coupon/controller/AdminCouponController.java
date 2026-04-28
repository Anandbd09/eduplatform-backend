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
@RequestMapping("/api/v1/admin/coupons")
public class AdminCouponController {

    @Autowired
    private CouponService couponService;

    /**
     * ENDPOINT 6: Create coupon (ADMIN)
     * POST /api/v1/admin/coupons
     */
    @PostMapping
    public ResponseEntity<?> createCoupon(
            @RequestBody CouponRequest request,
            @RequestHeader("X-User-Id") String adminId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            CouponResponse coupon = couponService.createCoupon(request, tenantId, adminId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Coupon created successfully", coupon));
        } catch (CouponException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error creating coupon", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to create coupon", null));
        }
    }

    /**
     * ENDPOINT 7: Get all coupons (ADMIN)
     * GET /api/v1/admin/coupons?page=0&size=10&status=ACTIVE
     */
    @GetMapping
    public ResponseEntity<?> getAllCoupons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            Page<CouponResponse> coupons = couponService.getAllCoupons(page, size, status, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Coupons retrieved", coupons));
        } catch (Exception e) {
            log.error("Error fetching coupons", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch coupons", null));
        }
    }

    /**
     * ENDPOINT 8: Get coupon by ID (ADMIN)
     * GET /api/v1/admin/coupons/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCouponById(
            @PathVariable String id,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            CouponResponse coupon = couponService.getCouponById(id, tenantId);
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
     * ENDPOINT 9: Update coupon (ADMIN)
     * PUT /api/v1/admin/coupons/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCoupon(
            @PathVariable String id,
            @RequestBody CouponRequest request,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            CouponResponse coupon = couponService.updateCoupon(id, request, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Coupon updated successfully", coupon));
        } catch (CouponException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error updating coupon", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to update coupon", null));
        }
    }

    /**
     * ENDPOINT 10: Delete coupon (ADMIN)
     * DELETE /api/v1/admin/coupons/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCoupon(
            @PathVariable String id,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            couponService.deleteCoupon(id, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Coupon archived successfully", null));
        } catch (CouponException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error deleting coupon", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to delete coupon", null));
        }
    }

    /**
     * ENDPOINT 11: Get coupon analytics (ADMIN)
     * GET /api/v1/admin/coupons/{id}/analytics
     */
    @GetMapping("/{id}/analytics")
    public ResponseEntity<?> getCouponAnalytics(
            @PathVariable String id,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            CouponAnalyticsResponse analytics = couponService.getCouponAnalytics(id, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Analytics retrieved", analytics));
        } catch (CouponException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error fetching analytics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch analytics", null));
        }
    }

    /**
     * ENDPOINT 12: Health check
     * GET /api/v1/admin/coupons/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Coupon service is healthy", null));
    }
}