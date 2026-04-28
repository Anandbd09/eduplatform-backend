package com.eduplatform.batch.controller;

import com.eduplatform.batch.service.BatchOperationService;
import com.eduplatform.batch.dto.*;
import com.eduplatform.batch.exception.BatchException;
import com.eduplatform.common.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/batch")
public class BatchController {

    @Autowired
    private BatchOperationService batchService;

    /**
     * ENDPOINT 1: Submit user import job
     * POST /api/v1/batch/import-users
     */
    @PostMapping("/import-users")
    public ResponseEntity<?> submitUserImport(
            @RequestParam MultipartFile file,
            @RequestParam String importType, // STUDENTS, INSTRUCTORS, ADMINS
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            BatchJobResponse response = batchService.submitUserImportJob(file, importType, userId, tenantId);
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(new ApiResponse<>(true, "Import job submitted", response));
        } catch (BatchException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error submitting user import", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to submit import", null));
        }
    }

    /**
     * ENDPOINT 2: Get job status
     * GET /api/v1/batch/jobs/{jobId}
     */
    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<?> getJobStatus(
            @PathVariable String jobId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            BatchJobResponse response = batchService.getJobStatus(jobId, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Job status retrieved", response));
        } catch (BatchException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error fetching job status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch status", null));
        }
    }

    /**
     * ENDPOINT 3: Get job results
     * GET /api/v1/batch/jobs/{jobId}/results?page=0&size=10
     */
    @GetMapping("/jobs/{jobId}/results")
    public ResponseEntity<?> getJobResults(
            @PathVariable String jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            Page<BatchResultResponse> results = batchService.getJobResults(jobId, page, size, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Results retrieved", results));
        } catch (Exception e) {
            log.error("Error fetching results", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch results", null));
        }
    }

    /**
     * ENDPOINT 4: Get my jobs
     * GET /api/v1/batch/my-jobs?page=0&size=10
     */
    @GetMapping("/my-jobs")
    public ResponseEntity<?> getMyJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            Page<BatchJobResponse> jobs = batchService.getUserJobs(userId, page, size, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Jobs retrieved", jobs));
        } catch (Exception e) {
            log.error("Error fetching user jobs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch jobs", null));
        }
    }

    /**
     * ENDPOINT 5: Cancel job
     * POST /api/v1/batch/jobs/{jobId}/cancel
     */
    @PostMapping("/jobs/{jobId}/cancel")
    public ResponseEntity<?> cancelJob(
            @PathVariable String jobId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            batchService.cancelJob(jobId, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Job cancelled", null));
        } catch (BatchException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error cancelling job", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to cancel job", null));
        }
    }
}