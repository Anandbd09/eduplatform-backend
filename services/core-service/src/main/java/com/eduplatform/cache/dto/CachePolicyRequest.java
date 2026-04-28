// FILE 16: CachePolicyRequest.java
package com.eduplatform.cache.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CachePolicyRequest {
    private String cacheType;
    private Integer defaultTtlSeconds;
    private Integer maxTtlSeconds;
    private String evictionPolicy;
    private Long maxEntriesInCache;
    private Boolean enabled;
}