// FILE 15: CacheStatisticsResponse.java
package com.eduplatform.cache.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CacheStatisticsResponse {
    private String cacheType;
    private Long totalEntries;
    private Long activeEntries;
    private Double hitRate;
    private Long memoryUsedMb;
}