package com.eduplatform.referral.controller;

import com.eduplatform.referral.service.*;
import com.eduplatform.referral.dto.*;
import com.eduplatform.common.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/referrals")
public class AdminReferralController {

    @Autowired
    private ReferralService referralService;

    @Autowired
    private RewardService rewardService;

    @Autowired
    private PayoutService payoutService;

    /**
     * ENDPOINT 7: Create referral code for instructor
     * POST /api/v1/admin/referrals/create
     */
    @PostMapping("/create")
    public ResponseEntity<?> createReferralCode(
            @RequestParam String instructorId,
            @RequestParam String instructorName,
            @RequestParam String instructorEmail,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            ReferralCodeResponse response = referralService.createReferralCode(
                    instructorId, instructorName, instructorEmail, tenantId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Referral code created", response));
        } catch (Exception e) {
            log.error("Error creating referral code", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to create referral code", null));
        }
    }

    /**
     * ENDPOINT 8: Approve rewards
     * POST /api/v1/admin/referrals/approve-rewards
     */
    @PostMapping("/approve-rewards")
    public ResponseEntity<?> approveRewards(
            @RequestBody java.util.List<String> rewardIds,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            rewardService.approveRewards(rewardIds);
            return ResponseEntity.ok(new ApiResponse<>(true, "Rewards approved", null));
        } catch (Exception e) {
            log.error("Error approving rewards", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to approve rewards", null));
        }
    }

    /**
     * ENDPOINT 9: Approve payout
     * POST /api/v1/admin/referrals/approve-payout/{payoutId}
     */
    @PostMapping("/approve-payout/{payoutId}")
    public ResponseEntity<?> approvePayout(
            @PathVariable String payoutId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            payoutService.approvePayout(payoutId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Payout approved", null));
        } catch (Exception e) {
            log.error("Error approving payout", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to approve payout", null));
        }
    }

    /**
     * ENDPOINT 10: Mark payout as paid
     * POST /api/v1/admin/referrals/mark-paid/{payoutId}
     */
    @PostMapping("/mark-paid/{payoutId}")
    public ResponseEntity<?> markAsPaid(
            @PathVariable String payoutId,
            @RequestParam String transactionId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            payoutService.markAsPaid(payoutId, transactionId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Payout marked as paid", null));
        } catch (Exception e) {
            log.error("Error marking payout as paid", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to mark payout as paid", null));
        }
    }

    /**
     * ENDPOINT 11: Get all referral codes
     * GET /api/v1/admin/referrals/codes?page=0&size=10&status=ACTIVE
     */
    @GetMapping("/codes")
    public ResponseEntity<?> getAllCodes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            return ResponseEntity.ok(new ApiResponse<>(true, "Codes retrieved", null));
        } catch (Exception e) {
            log.error("Error fetching codes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch codes", null));
        }
    }

    /**
     * ENDPOINT 12: Get health check
     * GET /api/v1/admin/referrals/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Referral service is healthy", null));
    }
}