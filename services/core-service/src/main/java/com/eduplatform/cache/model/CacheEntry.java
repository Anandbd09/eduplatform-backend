package com.eduplatform.cache.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "cache_entries")
public class CacheEntry {

    @Id
    private String id;

    @Indexed(unique = true)
    private String cacheKey; // e.g., "course:123", "user:456"

    @Indexed
    private String cacheType; // COURSE, USER, RECOMMENDATION, SESSION, etc

    private Object cachedValue; // JSON serialized

    private String valueHash; // SHA256 hash for change detection

    @Indexed
    private String status; // ACTIVE, EXPIRED, INVALID, EVICTED

    @Indexed
    private LocalDateTime createdAt;

    @Indexed
    private LocalDateTime expiresAt; // TTL expiry time

    private LocalDateTime lastAccessedAt;

    private Long accessCount; // How many times accessed

    private Integer ttlSeconds; // TTL in seconds

    private String sourceService; // Which service created this cache

    private Boolean isHot; // Frequently accessed (keep in memory)

    private Long sizeBytes; // Size for memory management

    @Indexed
    private String tenantId;

    private Long version = 0L;

    /**
     * Is cache entry still valid
     */
    public boolean isStillValid() {
        if ("INVALID".equals(status) || "EVICTED".equals(status)) {
            return false;
        }

        if (expiresAt != null && LocalDateTime.now().isAfter(expiresAt)) {
            return false;
        }

        return "ACTIVE".equals(status);
    }

    /**
     * Calculate cache efficiency score (0-100)
     */
    public double getEfficiencyScore() {
        if (accessCount == null || accessCount == 0) {
            return 0.0;
        }

        long daysOld = java.time.temporal.ChronoUnit.DAYS.between(createdAt, LocalDateTime.now());
        if (daysOld == 0) daysOld = 1;

        return (accessCount / (double) daysOld);
    }
}