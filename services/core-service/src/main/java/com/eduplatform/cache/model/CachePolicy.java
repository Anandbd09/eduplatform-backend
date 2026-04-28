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
@Document(collection = "cache_policies")
public class CachePolicy {

    @Id
    private String id;

    @Indexed(unique = true)
    private String cacheType; // COURSE, USER, RECOMMENDATION, SESSION

    private Integer defaultTtlSeconds; // Default TTL (1 hour = 3600)

    private Integer maxTtlSeconds; // Maximum allowed TTL (24 hours = 86400)

    private String evictionPolicy; // LRU, LFU, FIFO, TTL

    private Long maxEntriesInCache; // Max entries before eviction

    private Long maxMemoryMb; // Max memory before eviction

    private Boolean enabledCompressions; // Compress large values

    private String compressionType; // GZIP, SNAPPY

    private Boolean enabled; // Policy enabled/disabled

    private Boolean isHotData; // Keep in memory always

    private String invalidationTriggers; // Comma-separated: ORDER_PLACED, USER_UPDATED

    @Indexed
    private LocalDateTime createdAt;

    @Indexed
    private LocalDateTime updatedAt;

    @Indexed
    private String tenantId;

    private Long version = 0L;
}