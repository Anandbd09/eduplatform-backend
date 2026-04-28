package com.eduplatform.referral.controller;

import com.eduplatform.referral.service.ReferralService;
import com.eduplatform.referral.dto.*;
import com.eduplatform.referral.exception.ReferralException;
import com.eduplatform.common.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/referrals")
public class ReferralController {

    @Autowired
    private ReferralService referralService;

    /**
     * ENDPOINT 1: Get my referral code
     * GET /api/v1/referrals/my-code
     */
    @GetMapping("/my-code")
    public ResponseEntity<?> getMyReferralCode(
            @RequestHeader("X-User-Id") String instructorId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            // Get first active code for instructor
            Page<ReferralCodeResponse> codes = referralService.getInstructorCodes(instructorId, 0, 1, tenantId);

            if (codes.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "No referral code found", null));
            }

            return ResponseEntity.ok(new ApiResponse<>(true, "Referral code retrieved", codes.getContent().get(0)));
        } catch (Exception e) {
            log.error("Error fetching referral code", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch referral code", null));
        }
    }

    /**
     * ENDPOINT 2: Get pending rewards
     * GET /api/v1/referrals/rewards?page=0&size=10
     */
    @GetMapping("/rewards")
    public ResponseEntity<?> getPendingRewards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-User-Id") String instructorId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            Page<ReferralRewardResponse> rewards = referralService.getPendingRewards(instructorId, page, size, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Rewards retrieved", rewards));
        } catch (Exception e) {
            log.error("Error fetching rewards", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch rewards", null));
        }
    }

    /**
     * ENDPOINT 3: Request payout
     * POST /api/v1/referrals/request-payout
     */
    @PostMapping("/request-payout")
    public ResponseEntity<?> requestPayout(
            @RequestHeader("X-User-Id") String instructorId,
            @RequestHeader("X-User-Email") String instructorEmail,
            @RequestHeader("X-User-Name") String instructorName,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            ReferralPayoutResponse response = referralService.requestPayout(
                    instructorId, instructorName, instructorEmail, tenantId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Payout requested", response));
        } catch (ReferralException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error requesting payout", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to request payout", null));
        }
    }

    /**
     * ENDPOINT 4: Get my stats
     * GET /api/v1/referrals/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getStats(
            @RequestHeader("X-User-Id") String instructorId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            ReferralStatsResponse stats = referralService.getInstructorStats(instructorId, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Stats retrieved", stats));
        } catch (Exception e) {
            log.error("Error fetching stats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch stats", null));
        }
    }

    /**
     * ENDPOINT 5: Track referral click (PUBLIC)
     * POST /api/v1/referrals/track?code=REF-XXXXX&email=user@example.com
     */
    @PostMapping("/track")
    public ResponseEntity<?> trackClick(
            @RequestParam String code,
            @RequestParam(required = false) String email,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ip,
            @RequestHeader(value = "User-Agent", required = false) String userAgent,
            @RequestParam(required = false) String deviceType,
            @RequestParam(required = false) String country,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            String clientIp = ip != null ? ip.split(",")[0] : "0.0.0.0";

            referralService.trackReferralClick(code, email, clientIp, userAgent, deviceType, country, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Click tracked", null));
        } catch (ReferralException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error tracking click", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to track click", null));
        }
    }

    /**
     * ENDPOINT 6: Get leaderboard
     * GET /api/v1/referrals/leaderboard?limit=10
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<?> getLeaderboard(
            @RequestParam(defaultValue = "10") int limit,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            var leaderboard = referralService.getLeaderboard(Math.min(limit, 100), tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Leaderboard retrieved", leaderboard));
        } catch (Exception e) {
            log.error("Error fetching leaderboard", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch leaderboard", null));
        }
    }
}