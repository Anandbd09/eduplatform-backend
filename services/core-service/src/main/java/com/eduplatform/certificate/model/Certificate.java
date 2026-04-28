package com.eduplatform.certificate.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "certificates")
@CompoundIndex(name = "userId_courseId_idx", def = "{'userId': 1, 'courseId': 1, 'tenantId': 1}", unique = true)
public class Certificate {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String userName;

    private String userEmail;

    @Indexed
    private String courseId;

    private String courseName;

    @Indexed
    private String certificateNumber; // UNIQUE: CERT-2024-XXXXX

    @Indexed
    private String templateId;

    private String templateName;

    @Indexed
    private LocalDateTime issuedAt;

    @Indexed
    private LocalDateTime expiresAt; // 2 years from issuedAt

    @Indexed
    private String status; // ACTIVE, EXPIRED, REVOKED, DRAFT

    private String issuerName; // "EduPlatform"

    private String issuerSignature;

    private Map<String, String> customFields; // { "score": "95", "grade": "A" }

    private Double courseCompletion; // 0-100%

    private Integer totalLessons;

    private Integer completedLessons;

    private String verificationUrl; // /verify/{certificateNumber}

    @Indexed
    private String qrCodeUrl;

    @Indexed
    private LocalDateTime downloadedAt;

    private Integer downloadCount;

    @Indexed
    private String revocationReason; // null unless revoked

    @Indexed
    private LocalDateTime revokedAt;

    private String revokedBy;

    @Indexed
    private LocalDateTime createdAt;

    @Indexed
    private LocalDateTime updatedAt;

    @Indexed
    private String tenantId;

    private Long version = 0L;

    /**
     * Check if certificate is valid (not expired, not revoked)
     */
    public boolean isValid() {
        if ("REVOKED".equals(status)) {
            return false;
        }

        if ("EXPIRED".equals(status)) {
            return false;
        }

        if (expiresAt != null && LocalDateTime.now().isAfter(expiresAt)) {
            return false;
        }

        return "ACTIVE".equals(status);
    }

    /**
     * Check if certificate is expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Get days until expiration
     */
    public long getDaysUntilExpiration() {
        if (expiresAt == null) {
            return -1;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), expiresAt);
    }
}