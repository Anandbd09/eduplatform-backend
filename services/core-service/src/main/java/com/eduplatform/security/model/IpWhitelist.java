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
@Document(collection = "ip_whitelists")
public class IpWhitelist {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String ipAddress;

    private String cidrBlock; // e.g., 192.168.1.0/24

    private String description; // e.g., "Office Network"

    @Indexed
    private String status; // ACTIVE, INACTIVE, EXPIRED

    private Boolean isActive;

    @Indexed
    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    private Integer failedLoginAttempts;

    private LocalDateTime lastFailedAttemptAt;

    @Indexed
    private String tenantId;

    private Long version = 0L;

    /**
     * Is IP whitelist entry still active
     */
    public boolean isStillActive() {
        if (!Boolean.TRUE.equals(isActive)) {
            return false;
        }

        if (expiresAt != null && LocalDateTime.now().isAfter(expiresAt)) {
            return false;
        }

        return true;
    }
}