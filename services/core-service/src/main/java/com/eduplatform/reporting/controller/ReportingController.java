package com.eduplatform.reporting.controller;

import com.eduplatform.core.common.response.ApiResponse;
import com.eduplatform.reporting.dto.ReportRequest;
import com.eduplatform.reporting.dto.ReportResponse;
import com.eduplatform.reporting.dto.ReportingAnalytics;
import com.eduplatform.reporting.exception.ReportingException;
import com.eduplatform.reporting.service.ReportingService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/reporting")
public class ReportingController {

    private final ReportingService reportingService;

    public ReportingController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    /**
     * CREATE REPORT
     * POST /api/v1/reporting/reports
     */
    @PostMapping("/reports")
    public ResponseEntity<?> createReport(
            @RequestBody ReportRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            ReportResponse report = reportingService.createReport(request, userId, tenantId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(report, "Report created successfully"));
        } catch (ReportingException e) {
            return ResponseEntity.status(e.getHttpStatus())
                    .body(ApiResponse.error(e.getMessage(), e.getCode()));
        } catch (Exception e) {
            log.error("Error creating report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create report", "REPORT_CREATE_FAILED"));
        }
    }

    /**
     * GET REPORT
     * GET /api/v1/reporting/reports/{reportId}
     */
    @GetMapping("/reports/{reportId}")
    public ResponseEntity<?> getReport(
            @PathVariable String reportId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            ReportResponse report = reportingService.getReport(reportId, tenantId);
            return ResponseEntity.ok(ApiResponse.success(report, "Report retrieved"));
        } catch (ReportingException e) {
            return ResponseEntity.status(e.getHttpStatus())
                    .body(ApiResponse.error(e.getMessage(), e.getCode()));
        } catch (Exception e) {
            log.error("Error fetching report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch report", "REPORT_FETCH_FAILED"));
        }
    }

    /**
     * LIST REPORTS
     * GET /api/v1/reporting/reports?status=OPEN&page=0&size=10
     */
    @GetMapping("/reports")
    public ResponseEntity<?> listReports(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String severity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            Page<ReportResponse> reports = reportingService.getReports(page, size, status, category, severity, tenantId);
            return ResponseEntity.ok(ApiResponse.success(reports, "Reports retrieved"));
        } catch (Exception e) {
            log.error("Error fetching reports", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch reports", "REPORTS_FETCH_FAILED"));
        }
    }

    /**
     * MY REPORTS
     * GET /api/v1/reporting/my-reports?page=0&size=10
     */
    @GetMapping("/my-reports")
    public ResponseEntity<?> getMyReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            Page<ReportResponse> reports = reportingService.getMyReports(userId, page, size, tenantId);
            return ResponseEntity.ok(ApiResponse.success(reports, "Your reports retrieved"));
        } catch (Exception e) {
            log.error("Error fetching user reports", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch reports", "MY_REPORTS_FETCH_FAILED"));
        }
    }

    /**
     * ENTITY REPORTS
     * GET /api/v1/reporting/entities/{entityId}/reports?page=0&size=10
     */
    @GetMapping("/entities/{entityId}/reports")
    public ResponseEntity<?> getEntityReports(
            @PathVariable String entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            Page<ReportResponse> reports = reportingService.getEntityReports(entityId, page, size, tenantId);
            return ResponseEntity.ok(ApiResponse.success(reports, "Entity reports retrieved"));
        } catch (Exception e) {
            log.error("Error fetching entity reports", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch reports", "ENTITY_REPORTS_FETCH_FAILED"));
        }
    }

    /**
     * REPORT CATEGORIES
     * GET /api/v1/reporting/categories
     */
    @GetMapping("/categories")
    public ResponseEntity<?> getCategories(
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            // Return predefined categories
            String[] categories = {"PLAGIARISM", "FRAUD", "INAPPROPRIATE", "SPAM", "COPYRIGHT", "OTHER"};
            return ResponseEntity.ok(ApiResponse.success(categories, "Categories retrieved"));
        } catch (Exception e) {
            log.error("Error fetching categories", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch categories", "REPORT_CATEGORIES_FETCH_FAILED"));
        }
    }

    /**
     * ANALYTICS
     * GET /api/v1/reporting/analytics
     */
    @GetMapping("/analytics")
    public ResponseEntity<?> getAnalytics(
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            ReportingAnalytics analytics = reportingService.getAnalytics(tenantId);
            return ResponseEntity.ok(ApiResponse.success(analytics, "Analytics retrieved"));
        } catch (Exception e) {
            log.error("Error fetching analytics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch analytics", "REPORT_ANALYTICS_FETCH_FAILED"));
        }
    }
}
