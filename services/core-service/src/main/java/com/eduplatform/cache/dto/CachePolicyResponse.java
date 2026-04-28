// FILE 17: CachePolicyResponse.java
package com.eduplatform.cache.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CachePolicyResponse {
    private String cacheType;
    private Integer defaultTtlSeconds;
    private String evictionPolicy;
    private Long maxEntriesInCache;
    private Boolean enabled;
    private LocalDateTime createdAt;
}