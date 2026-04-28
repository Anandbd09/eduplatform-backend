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
@Document(collection = "device_sessions")
public class DeviceSession {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String deviceId;

    private String deviceName; // e.g., "iPhone 12", "Chrome on Windows"

    private String deviceType; // MOBILE, TABLET, DESKTOP, UNKNOWN

    private String ipAddress;

    private String country;

    private String userAgent;

    @Indexed
    private String status; // ACTIVE, INACTIVE, REVOKED

    private Boolean isTrusted;

    @Indexed
    private LocalDateTime createdAt;

    @Indexed
    private LocalDateTime lastActivityAt;

    private LocalDateTime revokedAt;

    @Indexed
    private String tenantId;

    private Long version = 0L;
}