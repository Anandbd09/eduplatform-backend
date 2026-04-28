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
@Document(collection = "monitoring_dashboards")
public class MonitoringDashboard {

    @Id
    private String id;

    @Indexed(unique = true)
    private String dashboardName;

    private String description;

    private Long totalRequests;

    private Long errorCount;

    private Double errorRate; // errors / total * 100

    private Long averageResponseTimeMs;

    private Long p95ResponseTimeMs; // 95th percentile

    private Long p99ResponseTimeMs; // 99th percentile

    private Double uptime; // Percentage

    private Long activeUsers;

    private Long databaseSize; // MB

    private Double cpuUsageAverage;

    private Double memoryUsageAverage;

    private Long throughputRequestsPerSecond;

    @Indexed
    private LocalDateTime lastUpdatedAt;

    @Indexed
    private String tenantId;

    private Long version_field = 0L;
}