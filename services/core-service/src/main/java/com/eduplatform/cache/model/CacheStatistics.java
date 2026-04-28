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
@Document(collection = "cache_statistics")
public class CacheStatistics {

    @Id
    private String id;

    @Indexed(unique = true)
    private String cacheType;

    private Long totalEntries;

    private Long activeEntries;

    private Long expiredEntries;

    private Long evictedEntries;

    private Long totalHits; // Cache hits

    private Long totalMisses; // Cache misses

    private Double hitRate; // hits / (hits + misses)

    private Long totalMemoryUsedBytes;

    private Long averageAccessCount;

    private Long averageTtlSeconds;

    private LocalDateTime lastUpdatedAt;

    @Indexed
    private String tenantId;

    private Long version = 0L;

    /**
     * Get cache hit ratio
     */
    public double getHitRatio() {
        long total = (totalHits != null ? totalHits : 0) + (totalMisses != null ? totalMisses : 0);
        if (total == 0) return 0.0;
        return ((double) (totalHits != null ? totalHits : 0) / total) * 100;
    }
}