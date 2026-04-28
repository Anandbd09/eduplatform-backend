package com.eduplatform.security.controller;

import com.eduplatform.common.ApiResponse;
import com.eduplatform.security.service.AdvancedSecurityService;
import com.eduplatform.security.dto.*;
import com.eduplatform.security.exception.SecurityException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/security")
public class SecurityController {

    @Autowired
    private AdvancedSecurityService securityService;

    /**
     * ENDPOINT 1: Setup 2FA
     * POST /api/v1/security/2fa/setup?method=TOTP
     */
    @PostMapping("/2fa/setup")
    public ResponseEntity<?> setupTwoFactorAuth(
            @RequestParam String method, // TOTP, SMS, EMAIL
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            TwoFactorSetupResponse response = securityService.setupTwoFactorAuth(userId, method, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "2FA setup initiated", response));
        } catch (SecurityException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error setting up 2FA", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to setup 2FA", null));
        }
    }

    /**
     * ENDPOINT 2: Verify 2FA OTP
     * POST /api/v1/security/2fa/verify
     */
    @PostMapping("/2fa/verify")
    public ResponseEntity<?> verifyTwoFactorSetup(
            @RequestBody OtpVerifyRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            securityService.verifyTwoFactorSetup(userId, request.getOtp(), tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "2FA enabled successfully", null));
        } catch (SecurityException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error verifying 2FA", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to verify 2FA", null));
        }
    }

    /**
     * ENDPOINT 3: Get security status
     * GET /api/v1/security/status
     */
    @GetMapping("/status")
    public ResponseEntity<?> getSecurityStatus(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            SecurityStatusResponse response = securityService.getSecurityStatus(userId, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Security status retrieved", response));
        } catch (Exception e) {
            log.error("Error fetching security status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch status", null));
        }
    }

    /**
     * ENDPOINT 4: Get login history
     * GET /api/v1/security/login-history?page=0&size=10
     */
    @GetMapping("/login-history")
    public ResponseEntity<?> getLoginHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            Page<LoginAttemptResponse> history = securityService.getLoginHistory(userId, page, size, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Login history retrieved", history));
        } catch (Exception e) {
            log.error("Error fetching login history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch history", null));
        }
    }
}