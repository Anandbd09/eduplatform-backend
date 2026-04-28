package com.eduplatform.otp.service;

import com.eduplatform.otp.model.*;
import com.eduplatform.otp.repository.*;
import com.eduplatform.otp.dto.*;
import com.eduplatform.otp.exception.OtpException;
import com.eduplatform.otp.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@Transactional
public class OtpService {

    @Autowired
    private OtpCodeRepository otpRepository;

    @Autowired
    private PhoneVerificationRepository phoneRepository;

    @Autowired
    private EmailVerificationRepository emailRepository;

    @Autowired
    private SmsOtpService smsService;

    @Autowired
    private EmailOtpService emailService;

    /**
     * SEND SMS OTP
     */
    public SendOtpResponse sendSmsOtp(SendOtpRequest request, String userId, String tenantId) {
        try {
            // Check rate limit (max 3 OTPs per hour per phone)
            RateLimiter.checkRateLimit(userId, "SMS", tenantId);

            // Generate 6-digit code
            String code = OtpGenerator.generate();

            // Create OTP record
            OtpCode otp = OtpCode.builder()
                    .id(UUID.randomUUID().toString())
                    .userId(userId)
                    .contactTarget(request.getPhoneNumber())
                    .code(code)
                    .type("SMS")
                    .purpose(request.getPurpose())
                    .status("ACTIVE")
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusMinutes(5))
                    .attemptCount(0)
                    .maxAttempts(3)
                    .isLocked(false)
                    .tenantId(tenantId)
                    .build();

            otpRepository.save(otp);

            // Send via Twilio
            smsService.sendSmsOtp(request.getPhoneNumber(), code, tenantId);

            log.info("SMS OTP sent: userId={}, phone={}", userId, request.getPhoneNumber());

            return SendOtpResponse.builder()
                    .otpId(otp.getId())
                    .type("SMS")
                    .contactTarget(maskPhoneNumber(request.getPhoneNumber()))
                    .expiresInSeconds(300)
                    .build();

        } catch (Exception e) {
            log.error("Error sending SMS OTP", e);
            throw new OtpException("Failed to send SMS OTP: " + e.getMessage());
        }
    }

    /**
     * SEND EMAIL OTP
     */
    public SendOtpResponse sendEmailOtp(SendOtpRequest request, String userId, String tenantId) {
        try {
            RateLimiter.checkRateLimit(userId, "EMAIL", tenantId);

            String code = OtpGenerator.generate();

            OtpCode otp = OtpCode.builder()
                    .id(UUID.randomUUID().toString())
                    .userId(userId)
                    .contactTarget(request.getEmailAddress())
                    .code(code)
                    .type("EMAIL")
                    .purpose(request.getPurpose())
                    .status("ACTIVE")
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusMinutes(5))
                    .attemptCount(0)
                    .maxAttempts(3)
                    .isLocked(false)
                    .tenantId(tenantId)
                    .build();

            otpRepository.save(otp);

            // Send via Resend
            emailService.sendEmailOtp(request.getEmailAddress(), code, tenantId);

            log.info("Email OTP sent: userId={}, email={}", userId, request.getEmailAddress());

            return SendOtpResponse.builder()
                    .otpId(otp.getId())
                    .type("EMAIL")
                    .contactTarget(maskEmailAddress(request.getEmailAddress()))
                    .expiresInSeconds(300)
                    .build();

        } catch (Exception e) {
            log.error("Error sending Email OTP", e);
            throw new OtpException("Failed to send Email OTP: " + e.getMessage());
        }
    }

    /**
     * VERIFY OTP
     */
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request, String userId, String tenantId) {
        try {
            // Find active OTP
            Optional<OtpCode> otpOpt = otpRepository.findByUserIdAndPurposeAndStatusAndTenantId(
                    userId, request.getPurpose(), "ACTIVE", tenantId);

            if (otpOpt.isEmpty()) {
                throw new OtpException("OTP not found or expired");
            }

            OtpCode otp = otpOpt.get();

            // Check if locked
            if (otp.getIsLocked()) {
                throw new OtpException("OTP locked after max attempts");
            }

            // Check if expired
            if (!otp.isStillValid()) {
                otp.setStatus("EXPIRED");
                otpRepository.save(otp);
                throw new OtpException("OTP expired");
            }

            // Increment attempt count
            otp.setAttemptCount(otp.getAttemptCount() + 1);

            // Check code match
            if (!otp.getCode().equals(request.getCode())) {
                // Check if max attempts exceeded
                if (otp.getAttemptCount() >= otp.getMaxAttempts()) {
                    otp.setIsLocked(true);
                    otp.setStatus("LOCKED");
                    otpRepository.save(otp);
                    throw new OtpException("OTP locked - max attempts exceeded");
                }
                otpRepository.save(otp);
                throw new OtpException("Invalid OTP code");
            }

            // Mark as verified
            otp.setStatus("VERIFIED");
            otp.setVerifiedAt(LocalDateTime.now());
            otpRepository.save(otp);

            // Update verification status
            if ("SMS".equals(otp.getType())) {
                updatePhoneVerification(userId, otp.getContactTarget(), tenantId);
            } else if ("EMAIL".equals(otp.getType())) {
                updateEmailVerification(userId, otp.getContactTarget(), tenantId);
            }

            log.info("OTP verified: userId={}, type={}, purpose={}", userId, otp.getType(), request.getPurpose());

            return VerifyOtpResponse.builder()
                    .success(true)
                    .message("OTP verified successfully")
                    .verificationToken(generateVerificationToken(userId, request.getPurpose(), tenantId))
                    .verifiedAt(otp.getVerifiedAt())
                    .build();

        } catch (OtpException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error verifying OTP", e);
            throw new OtpException("Failed to verify OTP");
        }
    }

    /**
     * RESEND OTP
     */
    public void resendOtp(ResendOtpRequest request, String userId, String tenantId) {
        try {
            // Find existing OTP
            Optional<OtpCode> otpOpt = otpRepository.findByUserIdAndPurposeAndStatusAndTenantId(
                    userId, request.getPurpose(), "ACTIVE", tenantId);

            if (otpOpt.isEmpty()) {
                throw new OtpException("No active OTP found");
            }

            OtpCode otp = otpOpt.get();

            // Check if locked
            if (otp.getIsLocked()) {
                throw new OtpException("OTP locked - cannot resend");
            }

            // Resend
            if ("SMS".equals(otp.getType())) {
                smsService.sendSmsOtp(otp.getContactTarget(), otp.getCode(), tenantId);
            } else {
                emailService.sendEmailOtp(otp.getContactTarget(), otp.getCode(), tenantId);
            }

            log.info("OTP resent: userId={}, type={}", userId, otp.getType());

        } catch (Exception e) {
            log.error("Error resending OTP", e);
            throw new OtpException("Failed to resend OTP");
        }
    }

    /**
     * GET VERIFICATION STATUS
     */
    public VerificationStatusResponse getVerificationStatus(String userId, String tenantId) {
        try {
            Optional<PhoneVerification> phone = phoneRepository.findByUserIdAndTenantId(userId, tenantId);
            Optional<EmailVerification> email = emailRepository.findByUserIdAndTenantId(userId, tenantId);

            return VerificationStatusResponse.builder()
                    .phoneVerified(phone.isPresent() && "VERIFIED".equals(phone.get().getStatus()))
                    .phoneNumber(phone.isPresent() ? maskPhoneNumber(phone.get().getPhoneNumber()) : null)
                    .phoneVerifiedAt(phone.isPresent() ? phone.get().getVerifiedAt() : null)
                    .emailVerified(email.isPresent() && "VERIFIED".equals(email.get().getStatus()))
                    .emailAddress(email.isPresent() ? maskEmailAddress(email.get().getEmailAddress()) : null)
                    .emailVerifiedAt(email.isPresent() ? email.get().getVerifiedAt() : null)
                    .build();

        } catch (Exception e) {
            log.error("Error getting verification status", e);
            throw new OtpException("Failed to get verification status");
        }
    }

    /**
     * HELPER: Update phone verification
     */
    private void updatePhoneVerification(String userId, String phoneNumber, String tenantId) {
        try {
            Optional<PhoneVerification> existing = phoneRepository.findByUserIdAndTenantId(userId, tenantId);

            PhoneVerification verification = existing.orElseGet(() -> PhoneVerification.builder()
                    .id(UUID.randomUUID().toString())
                    .userId(userId)
                    .tenantId(tenantId)
                    .build());

            verification.setPhoneNumber(phoneNumber);
            verification.setStatus("VERIFIED");
            verification.setVerifiedAt(LocalDateTime.now());
            verification.setVerificationCount((verification.getVerificationCount() != null ? verification.getVerificationCount() : 0) + 1);

            phoneRepository.save(verification);
        } catch (Exception e) {
            log.warn("Error updating phone verification", e);
        }
    }

    /**
     * HELPER: Update email verification
     */
    private void updateEmailVerification(String userId, String emailAddress, String tenantId) {
        try {
            Optional<EmailVerification> existing = emailRepository.findByUserIdAndTenantId(userId, tenantId);

            EmailVerification verification = existing.orElseGet(() -> EmailVerification.builder()
                    .id(UUID.randomUUID().toString())
                    .userId(userId)
                    .tenantId(tenantId)
                    .build());

            verification.setEmailAddress(emailAddress);
            verification.setStatus("VERIFIED");
            verification.setVerifiedAt(LocalDateTime.now());
            verification.setVerificationCount((verification.getVerificationCount() != null ? verification.getVerificationCount() : 0) + 1);

            emailRepository.save(verification);
        } catch (Exception e) {
            log.warn("Error updating email verification", e);
        }
    }

    /**
     * HELPER: Generate verification token
     */
    private String generateVerificationToken(String userId, String purpose, String tenantId) {
        return Base64.getEncoder().encodeToString((userId + ":" + purpose + ":" + System.currentTimeMillis()).getBytes());
    }

    /**
     * HELPER: Mask phone number
     */
    private String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() < 4) return phone;
        return "***" + phone.substring(phone.length() - 4);
    }

    /**
     * HELPER: Mask email address
     */
    private String maskEmailAddress(String email) {
        if (email == null || !email.contains("@")) return email;
        String[] parts = email.split("@");
        if (parts[0].length() < 2) return email;
        return parts[0].substring(0, 1) + "***@" + parts[1];
    }
}