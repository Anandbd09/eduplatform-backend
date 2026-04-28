// FILE 14: CacheEntryResponse.java
package com.eduplatform.cache.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CacheEntryResponse {
    private String cacheKey;
    private String cacheType;
    private String status;
    private Long accessCount;
    private Integer ttlSeconds;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}