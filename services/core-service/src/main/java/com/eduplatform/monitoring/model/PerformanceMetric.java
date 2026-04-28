package com.eduplatform.monitoring.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "performance_metrics")
public class PerformanceMetric {

    @Id
    private String id;

    @Indexed
    private String endpoint;

    @Indexed
    private LocalDateTime timestamp;

    private Long responseTimeMs;

    private Long memoryUsedMb;

    private Double cpuUsagePercent;

    private Long requestsPerMinute;

    private Long activeConnections;

    private Double jvmHeapUsagePercent;

    private Long gcCountPerMinute;

    private Long threadCount;

    private Long databaseQueryTimeMs;

    private Long cacheHitRate;

    private Long dbConnectionPoolSize;

    @Indexed
    private String tenantId;

    private Long version_field = 0L;

    /**
     * Is performance degraded
     */
    public boolean isDegraded() {
        return responseTimeMs != null && responseTimeMs > 1000 ||
                cpuUsagePercent != null && cpuUsagePercent > 80.0 ||
                jvmHeapUsagePercent != null && jvmHeapUsagePercent > 85.0;
    }
}