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
@Document(collection = "login_attempts")
public class LoginAttempt {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String email;

    private String ipAddress;

    private String deviceId;

    private String userAgent;

    private String country;

    private String city;

    @Indexed
    private String status; // SUCCESS, FAILED, BLOCKED, 2FA_REQUIRED

    private String failureReason; // INVALID_PASSWORD, ACCOUNT_LOCKED, etc

    private Boolean is2faRequired;

    private Boolean is2faVerified;

    @Indexed
    private LocalDateTime attemptedAt;

    private LocalDateTime verifiedAt;

    @Indexed
    private String tenantId;

    private Long version = 0L;
}