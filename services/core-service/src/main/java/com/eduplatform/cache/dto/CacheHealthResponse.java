// FILE 19: CacheHealthResponse.java
package com.eduplatform.cache.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CacheHealthResponse {
    private String status; // HEALTHY, DEGRADED
    private Long totalCacheEntries;
    private Double averageHitRate;
    private Long memoryUsedMb;
    private String message;
}