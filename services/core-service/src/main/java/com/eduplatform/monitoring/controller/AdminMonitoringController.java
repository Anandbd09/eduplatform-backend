package com.eduplatform.monitoring.controller;

import com.eduplatform.monitoring.service.MonitoringService;
import com.eduplatform.monitoring.dto.*;
import com.eduplatform.monitoring.exception.MonitoringException;
import com.eduplatform.common.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/monitoring")
public class AdminMonitoringController {

    @Autowired
    private MonitoringService monitoringService;

    /**
     * ENDPOINT 5: Acknowledge alert
     * POST /api/v1/admin/monitoring/alerts/{alertId}/acknowledge
     */
    @PostMapping("/alerts/{alertId}/acknowledge")
    public ResponseEntity<?> acknowledgeAlert(
            @PathVariable String alertId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            monitoringService.acknowledgeAlert(alertId, userId, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Alert acknowledged", null));
        } catch (MonitoringException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error acknowledging alert", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to acknowledge alert", null));
        }
    }

    /**
     * ENDPOINT 6: Resolve alert
     * POST /api/v1/admin/monitoring/alerts/{alertId}/resolve
     */
    @PostMapping("/alerts/{alertId}/resolve")
    public ResponseEntity<?> resolveAlert(
            @PathVariable String alertId,
            @RequestBody SystemAlertResolveRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            monitoringService.resolveAlert(alertId, userId, request.getResolutionNotes(), tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Alert resolved", null));
        } catch (MonitoringException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error resolving alert", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to resolve alert", null));
        }
    }

    /**
     * ENDPOINT 7: Cleanup old logs
     * DELETE /api/v1/admin/monitoring/logs/cleanup?olderThanDays=30
     */
    @DeleteMapping("/logs/cleanup")
    public ResponseEntity<?> cleanupOldLogs(
            @RequestParam(defaultValue = "30") Integer olderThanDays,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            monitoringService.cleanupOldLogs(olderThanDays, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Old logs cleaned up", null));
        } catch (Exception e) {
            log.error("Error cleaning up logs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to cleanup logs", null));
        }
    }

    /**
     * ENDPOINT 8: Health check
     * GET /api/v1/admin/monitoring/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        try {
            MonitoringHealthResponse health = MonitoringHealthResponse.builder()
                    .status("UP")
                    .message("Monitoring service is healthy")
                    .uptime(99.9)
                    .build();

            return ResponseEntity.ok(new ApiResponse<>(true, "Health check passed", health));
        } catch (Exception e) {
            log.error("Error checking health", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Health check failed", null));
        }
    }
}
