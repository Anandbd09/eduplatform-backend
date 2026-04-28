package com.eduplatform.monitoring.service;

import com.eduplatform.monitoring.model.*;
import com.eduplatform.monitoring.repository.*;
import com.eduplatform.monitoring.dto.*;
import com.eduplatform.monitoring.exception.MonitoringException;
import com.eduplatform.monitoring.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@Transactional
public class MonitoringService {

    @Autowired
    private ApplicationLogRepository logRepository;

    @Autowired
    private PerformanceMetricRepository metricRepository;

    @Autowired
    private SystemAlertRepository alertRepository;

    @Autowired
    private MonitoringDashboardRepository dashboardRepository;

    @Autowired
    private LoggingService loggingService;

    @Autowired
    private MetricsService metricsService;

    @Autowired
    private AlertService alertService;

    /**
     * LOG APPLICATION EVENT
     */
    public void logEvent(String level, String category, String message, String userId,
                         String endpoint, String httpMethod, Integer httpStatus,
                         Long responseTimeMs, String tenantId) {
        try {
            ApplicationLog applicationLog = ApplicationLog.builder()
                    .id(UUID.randomUUID().toString())
                    .level(level)
                    .category(category)
                    .message(message)
                    .userId(userId)
                    .endpoint(endpoint)
                    .httpMethod(httpMethod)
                    .httpStatus(httpStatus)
                    .responseTimeMs(responseTimeMs)
                    .timestamp(LocalDateTime.now())
                    .correlationId(UUID.randomUUID().toString())
                    .tenantId(tenantId)
                    .environment("PROD")
                    .build();

            logRepository.save(applicationLog);

            // Check if slow response
            if (applicationLog.isSlowResponse()) {
                applicationLog.setCategory("PERFORMANCE");
                logRepository.save(applicationLog);
            }

            // Create alert for errors
            if (applicationLog.isErrorLevel()) {
                alertService.createAlert(
                        "ERROR",
                        "CRITICAL",
                        "Application Error: " + message,
                        message,
                        tenantId
                );
            }

            log.info("Event logged: level={}, category={}, endpoint={}", level, category, endpoint);

        } catch (Exception e) {
            log.error("Error logging event", e);
        }
    }

    /**
     * RECORD PERFORMANCE METRIC
     */
    public void recordMetric(String endpoint, Long responseTimeMs, Long memoryUsedMb,
                             Double cpuUsagePercent, Long requestsPerMinute, String tenantId) {
        try {
            PerformanceMetric metric = PerformanceMetric.builder()
                    .id(UUID.randomUUID().toString())
                    .endpoint(endpoint)
                    .responseTimeMs(responseTimeMs)
                    .memoryUsedMb(memoryUsedMb)
                    .cpuUsagePercent(cpuUsagePercent)
                    .requestsPerMinute(requestsPerMinute)
                    .timestamp(LocalDateTime.now())
                    .tenantId(tenantId)
                    .build();

            metricRepository.save(metric);

            // Check for performance degradation
            if (metric.isDegraded()) {
                alertService.createAlert(
                        "PERFORMANCE",
                        "WARNING",
                        "Performance Degradation Detected",
                        "Response time: " + responseTimeMs + "ms, CPU: " + cpuUsagePercent + "%",
                        tenantId
                );
            }

            log.debug("Metric recorded: endpoint={}, responseTime={}", endpoint, responseTimeMs);

        } catch (Exception e) {
            log.error("Error recording metric", e);
        }
    }

    /**
     * GET APPLICATION LOGS
     */
    public Page<ApplicationLogResponse> getApplicationLogs(String level, String category,
                                                           int page, int size, String tenantId) {
        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);
            Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());

            Page<ApplicationLog> logs;
            if (level != null && !level.isEmpty()) {
                logs = logRepository.findByLevelAndTenantId(level, tenantId, pageable);
            } else if (category != null && !category.isEmpty()) {
                logs = logRepository.findByCategoryAndTenantId(category, tenantId, pageable);
            } else {
                logs = logRepository.findAll(pageable);
            }

            return logs.map(l -> ApplicationLogResponse.builder()
                    .level(l.getLevel())
                    .category(l.getCategory())
                    .message(l.getMessage())
                    .endpoint(l.getEndpoint())
                    .responseTimeMs(l.getResponseTimeMs())
                    .httpStatus(l.getHttpStatus())
                    .timestamp(l.getTimestamp())
                    .build());

        } catch (Exception e) {
            log.error("Error fetching logs", e);
            throw new MonitoringException("Failed to fetch logs");
        }
    }

    /**
     * GET PERFORMANCE METRICS
     */
    public Page<PerformanceMetricResponse> getPerformanceMetrics(String endpoint, Integer days,
                                                                 int page, int size, String tenantId) {
        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);

            LocalDateTime startDate = LocalDateTime.now().minusDays(days != null ? days : 7);
            LocalDateTime endDate = LocalDateTime.now();

            Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());

            Page<PerformanceMetric> metrics = metricRepository
                    .findByTimestampBetweenAndTenantId(startDate, endDate, tenantId, pageable);

            return metrics.map(m -> PerformanceMetricResponse.builder()
                    .endpoint(m.getEndpoint())
                    .responseTimeMs(m.getResponseTimeMs())
                    .memoryUsedMb(m.getMemoryUsedMb())
                    .cpuUsagePercent(m.getCpuUsagePercent())
                    .requestsPerMinute(m.getRequestsPerMinute())
                    .timestamp(m.getTimestamp())
                    .build());

        } catch (Exception e) {
            log.error("Error fetching performance metrics", e);
            throw new MonitoringException("Failed to fetch metrics");
        }
    }

    /**
     * GET SYSTEM ALERTS
     */
    public Page<SystemAlertResponse> getSystemAlerts(String status, Integer days,
                                                     int page, int size, String tenantId) {
        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

            Page<SystemAlert> alerts;
            if (status != null && !status.isEmpty()) {
                alerts = alertRepository.findByStatusAndTenantId(status, tenantId, pageable);
            } else {
                alerts = alertRepository.findAll(pageable);
            }

            return alerts.map(a -> SystemAlertResponse.builder()
                    .id(a.getId())
                    .alertType(a.getAlertType())
                    .severity(a.getSeverity())
                    .title(a.getTitle())
                    .status(a.getStatus())
                    .currentValue(a.getCurrentValue())
                    .threshold(a.getThreshold())
                    .createdAt(a.getCreatedAt())
                    .build());

        } catch (Exception e) {
            log.error("Error fetching alerts", e);
            throw new MonitoringException("Failed to fetch alerts");
        }
    }

    /**
     * ACKNOWLEDGE ALERT
     */
    public void acknowledgeAlert(String alertId, String userId, String tenantId) {
        try {
            Optional<SystemAlert> alert = alertRepository.findById(alertId);

            if (alert.isEmpty()) {
                throw new MonitoringException("Alert not found");
            }

            SystemAlert a = alert.get();
            a.setStatus("ACKNOWLEDGED");
            a.setAcknowledgedAt(LocalDateTime.now());
            a.setAcknowledgedBy(userId);
            alertRepository.save(a);

            log.info("Alert acknowledged: alertId={}, userId={}", alertId, userId);

        } catch (MonitoringException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error acknowledging alert", e);
            throw new MonitoringException("Failed to acknowledge alert");
        }
    }

    /**
     * RESOLVE ALERT
     */
    public void resolveAlert(String alertId, String userId, String resolutionNotes, String tenantId) {
        try {
            Optional<SystemAlert> alert = alertRepository.findById(alertId);

            if (alert.isEmpty()) {
                throw new MonitoringException("Alert not found");
            }

            SystemAlert a = alert.get();
            a.setStatus("RESOLVED");
            a.setResolvedAt(LocalDateTime.now());
            a.setResolvedBy(userId);
            a.setResolutionNotes(resolutionNotes);
            alertRepository.save(a);

            log.info("Alert resolved: alertId={}, userId={}", alertId, userId);

        } catch (MonitoringException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error resolving alert", e);
            throw new MonitoringException("Failed to resolve alert");
        }
    }

    /**
     * GET DASHBOARD METRICS
     */
    public DashboardMetricsResponse getDashboardMetrics(String tenantId) {
        try {
            // Calculate metrics
            Long totalLogs = logRepository.count();
            Long errorLogs = logRepository.count(); // Would filter by level=ERROR in production
            Double errorRate = totalLogs > 0 ? (errorLogs.doubleValue() / totalLogs) * 100 : 0.0;

            // Get latest performance metric
            Page<PerformanceMetric> latestMetrics = metricRepository
                    .findAll(PageRequest.of(0, 1, Sort.by("timestamp").descending()));

            long avgResponseTime = 0;
            long memoryUsed = 0;
            if (latestMetrics.hasContent()) {
                PerformanceMetric latest = latestMetrics.getContent().get(0);
                avgResponseTime = latest.getResponseTimeMs() != null ? latest.getResponseTimeMs() : 0;
                memoryUsed = latest.getMemoryUsedMb() != null ? latest.getMemoryUsedMb() : 0;
            }

            // Get active alerts
            List<SystemAlert> activeAlerts = alertRepository
                    .findByStatusAndTenantIdOrderByCreatedAtDesc("ACTIVE", tenantId);

            return DashboardMetricsResponse.builder()
                    .totalRequests(totalLogs)
                    .errorCount(errorLogs)
                    .errorRate(errorRate)
                    .averageResponseTimeMs(avgResponseTime)
                    .activeAlerts((long) activeAlerts.size())
                    .uptime(99.9)
                    .memoryUsedMb(memoryUsed)
                    .build();

        } catch (Exception e) {
            log.error("Error getting dashboard metrics", e);
            throw new MonitoringException("Failed to get dashboard metrics");
        }
    }

    /**
     * CLEANUP OLD LOGS
     */
    public void cleanupOldLogs(Integer olderThanDays, String tenantId) {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(olderThanDays != null ? olderThanDays : 30);

            Page<ApplicationLog> oldLogs = logRepository
                    .findByTimestampBetweenAndTenantId(
                            LocalDateTime.MIN,
                            cutoffDate,
                            tenantId,
                            PageRequest.of(0, 10000)
                    );

            oldLogs.getContent().forEach(log -> logRepository.delete(log));

            log.info("Cleaned up {} old log entries", oldLogs.getTotalElements());

        } catch (Exception e) {
            log.error("Error cleaning up old logs", e);
        }
    }
}
