package com.eduplatform.security.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "two_factor_auth")
public class TwoFactorAuth {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    private String method; // TOTP, SMS, EMAIL

    private Boolean isEnabled;

    private String secret; // Encrypted TOTP secret (Base32)

    private String phoneNumber; // For SMS 2FA

    private String backupCodes[]; // 10 backup codes

    private Integer backupCodesUsed;

    private LocalDateTime enabledAt;

    @Indexed
    private LocalDateTime lastVerifiedAt;

    private LocalDateTime lastBackupCodeUsedAt;

    private Boolean requiresVerification; // Pending verification

    private String temporarySecret; // For setup phase

    private LocalDateTime temporarySecretExpiresAt;

    private Integer failedAttempts;

    private LocalDateTime lastFailedAttemptAt;

    private Boolean isLocked; // Too many failed attempts

    private LocalDateTime lockedUntil;

    private Long recoveryCodeCount; // How many recovery codes left

    private String qrCodeUrl; // Stored QR code URL

    @Indexed
    private String tenantId;

    private Long version = 0L;

    /**
     * Is 2FA enabled and verified
     */
    public boolean isActiveAndVerified() {
        return Boolean.TRUE.equals(isEnabled) &&
                !Boolean.TRUE.equals(requiresVerification) &&
                secret != null;
    }

    /**
     * Is 2FA locked due to failed attempts
     */
    public boolean isLockedOut() {
        if (!Boolean.TRUE.equals(isLocked)) {
            return false;
        }
        return LocalDateTime.now().isBefore(lockedUntil);
    }
}