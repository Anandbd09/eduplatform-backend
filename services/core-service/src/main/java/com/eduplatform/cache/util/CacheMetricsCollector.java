// FILE 22: CacheMetricsCollector.java
package com.eduplatform.cache.util;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CacheMetricsCollector {
    private Long totalRequests;
    private Long cacheHits;
    private Long cacheMisses;
    private Long totalMemoryBytes;
    private Integer totalEntries;

    /**
     * GET HIT RATE
     */
    public double getHitRate() {
        if (totalRequests == null || totalRequests == 0) {
            return 0.0;
        }
        return ((double) (cacheHits != null ? cacheHits : 0) / totalRequests) * 100;
    }

    /**
     * GET MEMORY IN MB
     */
    public long getMemoryMb() {
        if (totalMemoryBytes == null) return 0;
        return totalMemoryBytes / (1024 * 1024);
    }
}