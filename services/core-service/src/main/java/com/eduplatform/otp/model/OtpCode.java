package com.eduplatform.otp.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "otp_codes")
public class OtpCode {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String contactTarget; // Phone or email

    private String code; // 6-digit code

    @Indexed
    private String type; // SMS, EMAIL

    @Indexed
    private String purpose; // PHONE_VERIFICATION, EMAIL_VERIFICATION, PASSWORD_RESET

    @Indexed
    private String status; // ACTIVE, VERIFIED, EXPIRED, LOCKED

    @Indexed
    private LocalDateTime createdAt;

    @Indexed
    private LocalDateTime expiresAt; // 5 minutes from creation

    private LocalDateTime verifiedAt;

    private Integer attemptCount; // Number of verification attempts

    private Integer maxAttempts; // Default 3

    private Boolean isLocked; // Locked after max attempts

    @Indexed
    private String tenantId;

    private Long version_field = 0L;

    /**
     * Is OTP still valid
     */
    public boolean isStillValid() {
        return "ACTIVE".equals(status) && LocalDateTime.now().isBefore(expiresAt);
    }

    /**
     * Can verify (not expired, not locked, not already verified)
     */
    public boolean canVerify() {
        return isStillValid() && !isLocked && attemptCount < maxAttempts;
    }
}