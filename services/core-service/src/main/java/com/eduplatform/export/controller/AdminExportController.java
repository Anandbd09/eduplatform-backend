package com.eduplatform.export.controller;

import com.eduplatform.common.ApiResponse;
import com.eduplatform.export.service.ExportService;
import com.eduplatform.export.dto.*;
import com.eduplatform.export.exception.ExportException;
//import com.eduplatform.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/export")
public class AdminExportController {

    @Autowired
    private ExportService exportService;

    /**
     * ENDPOINT 6: Create export template
     * POST /api/v1/admin/export/template
     */
    @PostMapping("/template")
    public ResponseEntity<?> createTemplate(
            @RequestBody ExportTemplateRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            ExportTemplateResponse response = exportService.createTemplate(request, userId, tenantId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Export template created", response));
        } catch (Exception e) {
            log.error("Error creating export template", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to create template", null));
        }
    }

    /**
     * ENDPOINT 7: Delete export job
     * DELETE /api/v1/admin/export/{jobId}
     */
    @DeleteMapping("/{jobId}")
    public ResponseEntity<?> deleteExport(
            @PathVariable String jobId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            exportService.deleteExport(jobId, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Export job deleted", null));
        } catch (Exception e) {
            log.error("Error deleting export", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to delete export", null));
        }
    }

    /**
     * ENDPOINT 8: Cleanup expired exports
     * POST /api/v1/admin/export/cleanup
     */
    @PostMapping("/cleanup")
    public ResponseEntity<?> cleanupExpired(
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            exportService.cleanupExpiredExports(tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Expired exports cleaned up", null));
        } catch (Exception e) {
            log.error("Error cleaning up exports", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to cleanup exports", null));
        }
    }

    /**
     * ENDPOINT 9: Get export audit log
     * GET /api/v1/admin/export/{jobId}/audit?page=0&size=10
     */
    @GetMapping("/{jobId}/audit")
    public ResponseEntity<?> getAuditLog(
            @PathVariable String jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            // Placeholder: Would fetch audit logs from service
            return ResponseEntity.ok(new ApiResponse<>(true, "Audit log retrieved", null));
        } catch (Exception e) {
            log.error("Error fetching audit log", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch audit log", null));
        }
    }

    /**
     * ENDPOINT 10: Export health check
     * GET /api/v1/admin/export/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        try {
            return ResponseEntity.ok(new ApiResponse<>(true, "Export service is healthy", null));
        } catch (Exception e) {
            log.error("Error checking export health", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Export service health check failed", null));
        }
    }
}