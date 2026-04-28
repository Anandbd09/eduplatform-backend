package com.eduplatform.monitoring.controller;

import com.eduplatform.monitoring.service.MonitoringService;
import com.eduplatform.monitoring.dto.*;
import com.eduplatform.monitoring.exception.MonitoringException;
import com.eduplatform.common.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/monitoring")
public class MonitoringController {

    @Autowired
    private MonitoringService monitoringService;

    /**
     * ENDPOINT 1: Get application logs
     * GET /api/v1/monitoring/logs?level=ERROR&page=0&size=10
     */
    @GetMapping("/logs")
    public ResponseEntity<?> getApplicationLogs(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            Page<ApplicationLogResponse> logs = monitoringService
                    .getApplicationLogs(level, category, page, size, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Application logs retrieved", logs));
        } catch (Exception e) {
            log.error("Error fetching logs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch logs", null));
        }
    }

    /**
     * ENDPOINT 2: Get performance metrics
     * GET /api/v1/monitoring/performance?endpoint=course&days=7&page=0&size=10
     */
    @GetMapping("/performance")
    public ResponseEntity<?> getPerformanceMetrics(
            @RequestParam(required = false) String endpoint,
            @RequestParam(defaultValue = "7") Integer days,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            Page<PerformanceMetricResponse> metrics = monitoringService
                    .getPerformanceMetrics(endpoint, days, page, size, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Performance metrics retrieved", metrics));
        } catch (Exception e) {
            log.error("Error fetching performance metrics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch metrics", null));
        }
    }

    /**
     * ENDPOINT 3: Get system alerts
     * GET /api/v1/monitoring/alerts?status=ACTIVE&page=0&size=10
     */
    @GetMapping("/alerts")
    public ResponseEntity<?> getSystemAlerts(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            Page<SystemAlertResponse> alerts = monitoringService
                    .getSystemAlerts(status, null, page, size, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "System alerts retrieved", alerts));
        } catch (Exception e) {
            log.error("Error fetching alerts", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch alerts", null));
        }
    }

    /**
     * ENDPOINT 4: Get dashboard metrics
     * GET /api/v1/monitoring/dashboard/metrics
     */
    @GetMapping("/dashboard/metrics")
    public ResponseEntity<?> getDashboardMetrics(
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            DashboardMetricsResponse metrics = monitoringService.getDashboardMetrics(tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Dashboard metrics retrieved", metrics));
        } catch (MonitoringException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error getting dashboard metrics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to get dashboard metrics", null));
        }
    }
}
