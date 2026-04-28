package com.eduplatform.security.service;

import com.eduplatform.security.model.*;
import com.eduplatform.security.repository.*;
import com.eduplatform.security.dto.*;
import com.eduplatform.security.exception.SecurityException;
import com.eduplatform.security.util.*;
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

@Slf4j
@Service
@Transactional
public class AdvancedSecurityService {

    @Autowired
    private TwoFactorAuthRepository twoFactorAuthRepository;

    @Autowired
    private LoginAttemptRepository loginAttemptRepository;

    @Autowired
    private SecurityAuditLogRepository auditLogRepository;

    @Autowired
    private IpWhitelistRepository ipWhitelistRepository;

    @Autowired
    private DeviceSessionRepository deviceSessionRepository;

    @Autowired
    private TwoFactorAuthService twoFactorService;

    @Autowired
    private LoginSecurityService loginSecurityService;

    @Autowired
    private IpWhitelistService ipWhitelistService;

    private static final Integer MAX_LOGIN_ATTEMPTS = 5;
    private static final Integer LOCKOUT_MINUTES = 30;

    /**
     * SETUP 2FA
     */
    public TwoFactorSetupResponse setupTwoFactorAuth(String userId, String method, String tenantId) {
        try {
            // Check if already enabled
            Optional<TwoFactorAuth> existing = twoFactorAuthRepository.findByUserIdAndTenantId(userId, tenantId);

            if (existing.isPresent() && existing.get().isActiveAndVerified()) {
                throw new SecurityException("2FA is already enabled for this user");
            }

            // Generate temporary secret
            String temporarySecret = OtpGenerator.generateBase32Secret();
            String qrCode = QrCodeGenerator.generateQrCode(userId, temporarySecret);

            // Create or update 2FA config
            TwoFactorAuth auth = existing.orElse(TwoFactorAuth.builder()
                    .id(UUID.randomUUID().toString())
                    .userId(userId)
                    .tenantId(tenantId)
                    .build());

            auth.setMethod(method);
            auth.setTemporarySecret(temporarySecret);
            auth.setTemporarySecretExpiresAt(LocalDateTime.now().plusHours(24));
            auth.setRequiresVerification(true);
            auth.setIsEnabled(false);

            TwoFactorAuth saved = twoFactorAuthRepository.save(auth);

            // Create audit log
            createAuditLog(userId, "2FA_SETUP_INITIATED", "2FA setup started with method: " + method,
                    null, "LOW", tenantId);

            log.info("2FA setup initiated: userId={}, method={}", userId, method);

            return TwoFactorSetupResponse.builder()
                    .userId(userId)
                    .method(method)
                    .secret(temporarySecret)
                    .qrCode(qrCode)
                    .requiresVerification(true)
                    .build();

        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error setting up 2FA", e);
            throw new SecurityException("Failed to setup 2FA: " + e.getMessage());
        }
    }

    /**
     * VERIFY 2FA SETUP
     */
    public void verifyTwoFactorSetup(String userId, String otp, String tenantId) {
        try {
            Optional<TwoFactorAuth> auth = twoFactorAuthRepository.findByUserIdAndTenantId(userId, tenantId);

            if (auth.isEmpty()) {
                throw new SecurityException("2FA not found");
            }

            TwoFactorAuth a = auth.get();

            // Check if temporary secret is expired
            if (a.getTemporarySecretExpiresAt() != null &&
                    LocalDateTime.now().isAfter(a.getTemporarySecretExpiresAt())) {
                throw new SecurityException("Setup expired. Start setup again.");
            }

            // Verify OTP
            if (!OtpGenerator.verifyTotp(a.getTemporarySecret(), otp)) {
                a.setFailedAttempts((a.getFailedAttempts() != null ? a.getFailedAttempts() : 0) + 1);

                if (a.getFailedAttempts() >= 5) {
                    a.setIsLocked(true);
                    a.setLockedUntil(LocalDateTime.now().plusMinutes(30));
                }

                twoFactorAuthRepository.save(a);
                throw new SecurityException("Invalid OTP");
            }

            // Generate backup codes
            List<String> backupCodes = generateBackupCodes(10);

            // Enable 2FA
            a.setSecret(EncryptionUtil.encrypt(a.getTemporarySecret()));
            a.setTemporarySecret(null);
            a.setTemporarySecretExpiresAt(null);
            a.setRequiresVerification(false);
            a.setIsEnabled(true);
            a.setEnabledAt(LocalDateTime.now());
            a.setLastVerifiedAt(LocalDateTime.now());
            a.setFailedAttempts(0);
            a.setIsLocked(false);
            a.setBackupCodes(backupCodes.toArray(new String[0]));
            a.setBackupCodesUsed(0);
            a.setRecoveryCodeCount((long) backupCodes.size());

            twoFactorAuthRepository.save(a);

            createAuditLog(userId, "2FA_ENABLED", "2FA successfully enabled with method: " + a.getMethod(),
                    null, "MEDIUM", tenantId);

            log.info("2FA enabled: userId={}", userId);

        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error verifying 2FA", e);
            throw new SecurityException("Failed to verify 2FA");
        }
    }

    /**
     * DISABLE 2FA
     */
    public void disableTwoFactorAuth(String userId, String password, String tenantId) {
        try {
            // In production: Verify password here

            Optional<TwoFactorAuth> auth = twoFactorAuthRepository.findByUserIdAndTenantId(userId, tenantId);

            if (auth.isEmpty()) {
                throw new SecurityException("2FA not found");
            }

            TwoFactorAuth a = auth.get();
            a.setIsEnabled(false);
            a.setSecret(null);
            a.setBackupCodes(null);
            a.setRecoveryCodeCount(0L);

            twoFactorAuthRepository.save(a);

            createAuditLog(userId, "2FA_DISABLED", "2FA disabled", null, "HIGH", tenantId);

            log.info("2FA disabled: userId={}", userId);

        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error disabling 2FA", e);
            throw new SecurityException("Failed to disable 2FA");
        }
    }

    /**
     * VERIFY 2FA OTP (DURING LOGIN)
     */
    public boolean verifyOtp(String userId, String otp, String tenantId) {
        try {
            Optional<TwoFactorAuth> auth = twoFactorAuthRepository.findByUserIdAndTenantId(userId, tenantId);

            if (auth.isEmpty() || !auth.get().isActiveAndVerified()) {
                return false;
            }

            TwoFactorAuth a = auth.get();

            // Check if locked
            if (a.isLockedOut()) {
                throw new SecurityException("2FA is locked. Try again later.");
            }

            String decryptedSecret = EncryptionUtil.decrypt(a.getSecret());

            if (OtpGenerator.verifyTotp(decryptedSecret, otp)) {
                a.setLastVerifiedAt(LocalDateTime.now());
                a.setFailedAttempts(0);
                twoFactorAuthRepository.save(a);
                return true;
            }

            // Failed attempt
            a.setFailedAttempts((a.getFailedAttempts() != null ? a.getFailedAttempts() : 0) + 1);
            a.setLastFailedAttemptAt(LocalDateTime.now());

            if (a.getFailedAttempts() >= 5) {
                a.setIsLocked(true);
                a.setLockedUntil(LocalDateTime.now().plusMinutes(30));
            }

            twoFactorAuthRepository.save(a);
            return false;

        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error verifying OTP", e);
            return false;
        }
    }

    /**
     * RECORD LOGIN ATTEMPT
     */
    public void recordLoginAttempt(String userId, String email, String ipAddress, String deviceId,
                                   String userAgent, String status, String failureReason, String tenantId) {
        try {
            LoginAttempt attempt = LoginAttempt.builder()
                    .id(UUID.randomUUID().toString())
                    .userId(userId)
                    .email(email)
                    .ipAddress(ipAddress)
                    .deviceId(deviceId)
                    .userAgent(userAgent)
                    .status(status)
                    .failureReason(failureReason)
                    .attemptedAt(LocalDateTime.now())
                    .tenantId(tenantId)
                    .build();

            loginAttemptRepository.save(attempt);

            // Check for lockout
            if ("FAILED".equals(status)) {
                Long failedAttempts = loginAttemptRepository.countByUserIdAndStatusAndAttemptedAtAfterAndTenantId(
                        userId, "FAILED", LocalDateTime.now().minusMinutes(LOCKOUT_MINUTES), tenantId);

                if (failedAttempts >= MAX_LOGIN_ATTEMPTS) {
                    createAuditLog(userId, "LOGIN_ATTEMPTS_EXCEEDED",
                            "Account locked due to multiple failed login attempts",
                            ipAddress, "CRITICAL", tenantId);
                }
            }

        } catch (Exception e) {
            log.error("Error recording login attempt", e);
        }
    }

    /**
     * ADD DEVICE SESSION
     */
    public void addDeviceSession(String userId, String deviceId, String deviceName, String deviceType,
                                 String ipAddress, String country, String userAgent, String tenantId) {
        try {
            Optional<DeviceSession> existing = deviceSessionRepository
                    .findByUserIdAndDeviceIdAndTenantId(userId, deviceId, tenantId);

            if (existing.isPresent()) {
                DeviceSession ds = existing.get();
                ds.setLastActivityAt(LocalDateTime.now());
                ds.setStatus("ACTIVE");
                deviceSessionRepository.save(ds);
            } else {
                DeviceSession session = DeviceSession.builder()
                        .id(UUID.randomUUID().toString())
                        .userId(userId)
                        .deviceId(deviceId)
                        .deviceName(deviceName)
                        .deviceType(deviceType)
                        .ipAddress(ipAddress)
                        .country(country)
                        .userAgent(userAgent)
                        .status("ACTIVE")
                        .createdAt(LocalDateTime.now())
                        .lastActivityAt(LocalDateTime.now())
                        .tenantId(tenantId)
                        .build();

                deviceSessionRepository.save(session);
            }

        } catch (Exception e) {
            log.error("Error adding device session", e);
        }
    }

    /**
     * GET SECURITY STATUS
     */
    public SecurityStatusResponse getSecurityStatus(String userId, String tenantId) {
        try {
            Optional<TwoFactorAuth> twoFa = twoFactorAuthRepository.findByUserIdAndTenantId(userId, tenantId);
            List<IpWhitelist> whitelist = ipWhitelistRepository
                    .findByUserIdAndStatusAndTenantId(userId, "ACTIVE", tenantId);
            List<DeviceSession> devices = deviceSessionRepository
                    .findByUserIdAndStatusAndTenantId(userId, "ACTIVE", tenantId);

            return SecurityStatusResponse.builder()
                    .twoFactorEnabled(twoFa.isPresent() && twoFa.get().isActiveAndVerified())
                    .twoFactorMethod(twoFa.map(TwoFactorAuth::getMethod).orElse(null))
                    .whitelistedIpsCount((long) whitelist.size())
                    .activeDevicesCount((long) devices.size())
                    .backupCodesRemaining(twoFa.map(TwoFactorAuth::getRecoveryCodeCount).orElse(0L))
                    .build();

        } catch (Exception e) {
            log.error("Error getting security status", e);
            throw new SecurityException("Failed to get security status");
        }
    }

    /**
     * ADD IP TO WHITELIST
     */
    public IpWhitelistResponse addIpToWhitelist(String userId, String ipAddress, String description, String tenantId) {
        try {
            IpWhitelist whitelist = IpWhitelist.builder()
                    .id(UUID.randomUUID().toString())
                    .userId(userId)
                    .ipAddress(ipAddress)
                    .description(description)
                    .status("ACTIVE")
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusDays(365))
                    .failedLoginAttempts(0)
                    .tenantId(tenantId)
                    .build();

            IpWhitelist saved = ipWhitelistRepository.save(whitelist);

            createAuditLog(userId, "IP_WHITELISTED", "IP added to whitelist: " + ipAddress,
                    ipAddress, "MEDIUM", tenantId);

            log.info("IP whitelisted: userId={}, ip={}", userId, ipAddress);

            return IpWhitelistResponse.builder()
                    .ipAddress(saved.getIpAddress())
                    .description(saved.getDescription())
                    .status(saved.getStatus())
                    .createdAt(saved.getCreatedAt())
                    .expiresAt(saved.getExpiresAt())
                    .build();

        } catch (Exception e) {
            log.error("Error adding IP to whitelist", e);
            throw new SecurityException("Failed to add IP to whitelist");
        }
    }

    /**
     * REMOVE IP FROM WHITELIST
     */
    public void removeIpFromWhitelist(String userId, String whitelistId, String tenantId) {
        try {
            Optional<IpWhitelist> whitelist = ipWhitelistRepository.findById(whitelistId);

            if (whitelist.isEmpty()) {
                throw new SecurityException("IP whitelist not found");
            }

            IpWhitelist w = whitelist.get();
            w.setStatus("INACTIVE");
            w.setIsActive(false);
            ipWhitelistRepository.save(w);

            createAuditLog(userId, "IP_REMOVED", "IP removed from whitelist",
                    w.getIpAddress(), "MEDIUM", tenantId);

            log.info("IP removed from whitelist: userId={}, ip={}", userId, w.getIpAddress());

        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error removing IP from whitelist", e);
            throw new SecurityException("Failed to remove IP from whitelist");
        }
    }

    /**
     * REVOKE DEVICE
     */
    public void revokeDevice(String userId, String deviceId, String tenantId) {
        try {
            Optional<DeviceSession> device = deviceSessionRepository
                    .findByUserIdAndDeviceIdAndTenantId(userId, deviceId, tenantId);

            if (device.isEmpty()) {
                throw new SecurityException("Device not found");
            }

            DeviceSession d = device.get();
            d.setStatus("REVOKED");
            d.setRevokedAt(LocalDateTime.now());
            deviceSessionRepository.save(d);

            createAuditLog(userId, "DEVICE_REVOKED", "Device revoked: " + d.getDeviceName(),
                    d.getIpAddress(), "HIGH", tenantId);

            log.info("Device revoked: userId={}, deviceId={}", userId, deviceId);

        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error revoking device", e);
            throw new SecurityException("Failed to revoke device");
        }
    }

    /**
     * GET LOGIN HISTORY
     */
    public Page<LoginAttemptResponse> getLoginHistory(String userId, int page, int size, String tenantId) {
        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);
            Pageable pageable = PageRequest.of(page, size, Sort.by("attemptedAt").descending());

            Page<LoginAttempt> attempts = loginAttemptRepository.findByUserIdAndTenantId(userId, tenantId, pageable);

            return attempts.map(a -> LoginAttemptResponse.builder()
                    .status(a.getStatus())
                    .ipAddress(a.getIpAddress())
                    .country(a.getCountry())
                    .deviceId(a.getDeviceId())
                    .attemptedAt(a.getAttemptedAt())
                    .build());

        } catch (Exception e) {
            log.error("Error fetching login history", e);
            throw new SecurityException("Failed to fetch login history");
        }
    }

    /**
     * HELPER: Generate backup codes
     */
    private List<String> generateBackupCodes(int count) {
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            codes.add(OtpGenerator.generateBackupCode());
        }
        return codes;
    }

    /**
     * HELPER: Create audit log
     */
    private void createAuditLog(String userId, String action, String details, String ipAddress,
                                String riskLevel, String tenantId) {
        try {
            SecurityAuditLog log = SecurityAuditLog.builder()
                    .id(UUID.randomUUID().toString())
                    .userId(userId)
                    .action(action)
                    .details(details)
                    .ipAddress(ipAddress)
                    .riskLevel(riskLevel)
                    .timestamp(LocalDateTime.now())
                    .tenantId(tenantId)
                    .build();

            auditLogRepository.save(log);
        } catch (Exception e) {
            log.error("Error creating audit log", e);
        }
    }
}