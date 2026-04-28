// FILE 18: CacheInvalidateRequest.java
package com.eduplatform.cache.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CacheInvalidateRequest {
    private String cacheKey;
    private String cacheType; // If invalidating by type
    private String trigger; // e.g., ORDER_PLACED
}