package com.eduplatform.export.controller;

import com.eduplatform.common.ApiResponse;
import com.eduplatform.export.service.ExportService;
import com.eduplatform.export.dto.*;
import com.eduplatform.export.exception.ExportException;
//import com.eduplatform.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/export")
public class ExportController {

    @Autowired
    private ExportService exportService;

    /**
     * ENDPOINT 1: Create export job
     * POST /api/v1/export/create
     */
    @PostMapping("/create")
    public ResponseEntity<?> createExport(
            @RequestBody ExportJobRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            ExportJobResponse response = exportService.createExportJob(request, userId, tenantId);
            return ResponseEntity.accepted()
                    .body(new ApiResponse<>(true, "Export job created and queued", response));
        } catch (ExportException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error creating export", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to create export", null));
        }
    }

    /**
     * ENDPOINT 2: Get export status
     * GET /api/v1/export/{jobId}/status
     */
    @GetMapping("/{jobId}/status")
    public ResponseEntity<?> getExportStatus(
            @PathVariable String jobId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            ExportStatusResponse status = exportService.getExportStatus(jobId, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Export status retrieved", status));
        } catch (ExportException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error getting export status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to get export status", null));
        }
    }

    /**
     * ENDPOINT 3: Download export file
     * GET /api/v1/export/{jobId}/download
     */
    @GetMapping("/{jobId}/download")
    public ResponseEntity<?> downloadExport(
            @PathVariable String jobId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            ExportDownloadResponse response = exportService.downloadExport(jobId, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Export file ready for download", response));
        } catch (ExportException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error downloading export", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to download export", null));
        }
    }

    /**
     * ENDPOINT 4: Get export history
     * GET /api/v1/export/history?page=0&size=10
     */
    @GetMapping("/history")
    public ResponseEntity<?> getExportHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            Page<ExportJobResponse> history = exportService.getExportHistory(userId, page, size, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Export history retrieved", history));
        } catch (Exception e) {
            log.error("Error fetching export history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch history", null));
        }
    }

    /**
     * ENDPOINT 5: GDPR data export
     * POST /api/v1/export/gdpr
     */
    @PostMapping("/gdpr")
    public ResponseEntity<?> gdprDataExport(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            ExportJobResponse response = exportService.gdprDataExport(userId, tenantId);
            return ResponseEntity.accepted()
                    .body(new ApiResponse<>(true, "GDPR data export initiated", response));
        } catch (ExportException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error creating GDPR export", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to create GDPR export", null));
        }
    }
}