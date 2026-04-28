package com.eduplatform.otp.controller;

import com.eduplatform.otp.service.OtpService;
import com.eduplatform.otp.dto.*;
import com.eduplatform.otp.exception.OtpException;
import com.eduplatform.common.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/otp")
public class OtpController {

    @Autowired
    private OtpService otpService;

    /**
     * ENDPOINT 1: Send SMS OTP
     * POST /api/v1/otp/send/sms
     */
    @PostMapping("/send/sms")
    public ResponseEntity<?> sendSmsOtp(
            @RequestBody SendOtpRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            SendOtpResponse response = otpService.sendSmsOtp(request, userId, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "SMS OTP sent", response));
        } catch (OtpException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error sending SMS OTP", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to send SMS OTP", null));
        }
    }

    /**
     * ENDPOINT 2: Send Email OTP
     * POST /api/v1/otp/send/email
     */
    @PostMapping("/send/email")
    public ResponseEntity<?> sendEmailOtp(
            @RequestBody SendOtpRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            SendOtpResponse response = otpService.sendEmailOtp(request, userId, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Email OTP sent", response));
        } catch (OtpException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error sending Email OTP", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to send Email OTP", null));
        }
    }

    /**
     * ENDPOINT 3: Verify OTP
     * POST /api/v1/otp/verify
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtp(
            @RequestBody VerifyOtpRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            VerifyOtpResponse response = otpService.verifyOtp(request, userId, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "OTP verified", response));
        } catch (OtpException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error verifying OTP", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to verify OTP", null));
        }
    }

    /**
     * ENDPOINT 4: Resend OTP
     * POST /api/v1/otp/resend
     */
    @PostMapping("/resend")
    public ResponseEntity<?> resendOtp(
            @RequestBody ResendOtpRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            otpService.resendOtp(request, userId, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "OTP resent", null));
        } catch (OtpException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error resending OTP", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to resend OTP", null));
        }
    }

    /**
     * ENDPOINT 5: Get verification status
     * GET /api/v1/otp/status
     */
    @GetMapping("/status")
    public ResponseEntity<?> getVerificationStatus(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            VerificationStatusResponse status = otpService.getVerificationStatus(userId, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Verification status retrieved", status));
        } catch (Exception e) {
            log.error("Error getting verification status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to get verification status", null));
        }
    }

    /**
     * ENDPOINT 6: Health check
     * GET /api/v1/otp/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(new ApiResponse<>(true, "OTP service is healthy", null));
    }
}
