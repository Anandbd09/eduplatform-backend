// FILE 13: CacheEntryRequest.java
package com.eduplatform.cache.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CacheEntryRequest {
    private String cacheKey;
    private Object value;
    private String cacheType;
    private Integer ttlSeconds;
}