package com.eduplatform.reporting.controller;

import com.eduplatform.core.common.response.ApiResponse;
import com.eduplatform.reporting.dto.AdminNotesRequest;
import com.eduplatform.reporting.dto.DisputeRequest;
import com.eduplatform.reporting.dto.DisputeResolutionRequest;
import com.eduplatform.reporting.dto.DisputeResolutionResponse;
import com.eduplatform.reporting.dto.DisputeResponse;
import com.eduplatform.reporting.exception.ReportingException;
import com.eduplatform.reporting.service.AdminReviewService;
import com.eduplatform.reporting.service.DisputeService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/disputes")
public class AdminReviewController {

    private final DisputeService disputeService;
    private final AdminReviewService adminReviewService;

    public AdminReviewController(DisputeService disputeService, AdminReviewService adminReviewService) {
        this.disputeService = disputeService;
        this.adminReviewService = adminReviewService;
    }

    /**
     * CREATE DISPUTE FROM REPORT
     * POST /api/v1/admin/disputes
     */
    @PostMapping
    public ResponseEntity<?> createDispute(
            @RequestBody DisputeRequest request,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            DisputeResponse dispute = disputeService.createDispute(request, tenantId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(dispute, "Dispute created"));
        } catch (ReportingException e) {
            return ResponseEntity.status(e.getHttpStatus())
                    .body(ApiResponse.error(e.getMessage(), e.getCode()));
        } catch (Exception e) {
            log.error("Error creating dispute", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create dispute", "DISPUTE_CREATE_FAILED"));
        }
    }

    /**
     * GET ADMIN QUEUE
     * GET /api/v1/admin/disputes/queue?page=0&size=10
     */
    @GetMapping("/queue")
    public ResponseEntity<?> getQueue(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            Page<DisputeResponse> queue = disputeService.getAdminQueue(page, size, tenantId);
            return ResponseEntity.ok(ApiResponse.success(queue, "Queue retrieved"));
        } catch (Exception e) {
            log.error("Error fetching queue", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch queue", "DISPUTE_QUEUE_FETCH_FAILED"));
        }
    }

    /**
     * ASSIGN DISPUTE
     * PUT /api/v1/admin/disputes/{disputeId}/assign?adminId=X&adminName=Y
     */
    @PutMapping("/{disputeId}/assign")
    public ResponseEntity<?> assignDispute(
            @PathVariable String disputeId,
            @RequestParam String adminId,
            @RequestParam String adminName,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            DisputeResponse dispute = disputeService.assignDispute(disputeId, adminId, adminName, tenantId);
            return ResponseEntity.ok(ApiResponse.success(dispute, "Dispute assigned"));
        } catch (ReportingException e) {
            return ResponseEntity.status(e.getHttpStatus())
                    .body(ApiResponse.error(e.getMessage(), e.getCode()));
        } catch (Exception e) {
            log.error("Error assigning dispute", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to assign dispute", "DISPUTE_ASSIGN_FAILED"));
        }
    }

    /**
     * GET MY DISPUTES
     * GET /api/v1/admin/disputes/my-disputes?page=0&size=10
     */
    @GetMapping("/my-disputes")
    public ResponseEntity<?> getMyDisputes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-User-Id") String adminId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            Page<DisputeResponse> disputes = disputeService.getAdminDisputes(adminId, page, size, tenantId);
            return ResponseEntity.ok(ApiResponse.success(disputes, "Disputes retrieved"));
        } catch (Exception e) {
            log.error("Error fetching disputes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch disputes", "ADMIN_DISPUTES_FETCH_FAILED"));
        }
    }

    /**
     * UPDATE ADMIN NOTES
     * PUT /api/v1/admin/disputes/{disputeId}/notes
     */
    @PutMapping("/{disputeId}/notes")
    public ResponseEntity<?> updateNotes(
            @PathVariable String disputeId,
            @RequestBody AdminNotesRequest request,
            @RequestHeader("X-User-Id") String adminId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            DisputeResponse dispute = disputeService.updateAdminNotes(disputeId, request.getNotes(), adminId, tenantId);
            return ResponseEntity.ok(ApiResponse.success(dispute, "Notes updated"));
        } catch (ReportingException e) {
            return ResponseEntity.status(e.getHttpStatus())
                    .body(ApiResponse.error(e.getMessage(), e.getCode()));
        } catch (Exception e) {
            log.error("Error updating notes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update notes", "DISPUTE_NOTES_UPDATE_FAILED"));
        }
    }

    /**
     * RESOLVE DISPUTE
     * POST /api/v1/admin/disputes/{disputeId}/resolution
     */
    @PostMapping("/{disputeId}/resolution")
    public ResponseEntity<?> resolveDispute(
            @PathVariable String disputeId,
            @RequestBody DisputeResolutionRequest request,
            @RequestHeader("X-User-Id") String adminId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            DisputeResolutionResponse resolution = adminReviewService.resolveDispute(disputeId, request, adminId, tenantId);
            return ResponseEntity.ok(ApiResponse.success(resolution, "Dispute resolved"));
        } catch (ReportingException e) {
            return ResponseEntity.status(e.getHttpStatus())
                    .body(ApiResponse.error(e.getMessage(), e.getCode()));
        } catch (Exception e) {
            log.error("Error resolving dispute", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to resolve dispute", "DISPUTE_RESOLVE_FAILED"));
        }
    }

    /**
     * GET OVERDUE DISPUTES
     * GET /api/v1/admin/disputes/overdue
     */
    @GetMapping("/overdue")
    public ResponseEntity<?> getOverdueDisputes(
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            var overdue = disputeService.getOverdueDisputes(tenantId);
            return ResponseEntity.ok(ApiResponse.success(overdue, "Overdue disputes retrieved"));
        } catch (Exception e) {
            log.error("Error fetching overdue disputes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch overdue disputes", "OVERDUE_DISPUTES_FETCH_FAILED"));
        }
    }

    /**
     * GET RESOLUTION
     * GET /api/v1/admin/disputes/{disputeId}/resolution
     */
    @GetMapping("/{disputeId}/resolution")
    public ResponseEntity<?> getResolution(
            @PathVariable String disputeId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            DisputeResolutionResponse resolution = adminReviewService.getResolution(disputeId, tenantId);
            return ResponseEntity.ok(ApiResponse.success(resolution, "Resolution retrieved"));
        } catch (ReportingException e) {
            return ResponseEntity.status(e.getHttpStatus())
                    .body(ApiResponse.error(e.getMessage(), e.getCode()));
        } catch (Exception e) {
            log.error("Error fetching resolution", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch resolution", "DISPUTE_RESOLUTION_FETCH_FAILED"));
        }
    }
}
