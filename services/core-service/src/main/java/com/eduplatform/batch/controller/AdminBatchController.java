package com.eduplatform.batch.controller;

import com.eduplatform.batch.service.BatchOperationService;
import com.eduplatform.batch.dto.*;
import com.eduplatform.common.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/batch")
public class AdminBatchController {

    @Autowired
    private BatchOperationService batchService;

    /**
     * ENDPOINT 6: Submit course assignment job
     * POST /api/v1/admin/batch/assign-courses
     */
    @PostMapping("/assign-courses")
    public ResponseEntity<?> submitCourseAssignment(
            @RequestBody CourseAssignmentRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            BatchJobResponse response = batchService.submitCourseAssignmentJob(request, userId, tenantId);
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(new ApiResponse<>(true, "Assignment job submitted", response));
        } catch (Exception e) {
            log.error("Error submitting assignment job", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to submit assignment", null));
        }
    }

    /**
     * ENDPOINT 7: Submit batch enrollment
     * POST /api/v1/admin/batch/enroll-batch
     */
    @PostMapping("/enroll-batch")
    public ResponseEntity<?> submitEnrollmentBatch(
            @RequestBody EnrollmentBatchRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            BatchJobResponse response = batchService.submitEnrollmentBatchJob(
                    request.getUserIds(), request.getCourseIds(), userId, tenantId);
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(new ApiResponse<>(true, "Enrollment batch submitted", response));
        } catch (Exception e) {
            log.error("Error submitting enrollment batch", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to submit enrollment", null));
        }
    }

    /**
     * ENDPOINT 8: Get failed results
     * GET /api/v1/admin/batch/jobs/{jobId}/failed?page=0&size=10
     */
    @GetMapping("/jobs/{jobId}/failed")
    public ResponseEntity<?> getFailedResults(
            @PathVariable String jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            Page<BatchResultResponse> results = batchService.getFailedResults(jobId, page, size, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Failed results retrieved", results));
        } catch (Exception e) {
            log.error("Error fetching failed results", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch results", null));
        }
    }

    /**
     * ENDPOINT 9: Get all jobs by type
     * GET /api/v1/admin/batch/jobs?type=USER_IMPORT&page=0&size=10
     */
    @GetMapping("/jobs")
    public ResponseEntity<?> getAllJobs(
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            // In production: Implement method to get jobs by type
            return ResponseEntity.ok(new ApiResponse<>(true, "Jobs retrieved", null));
        } catch (Exception e) {
            log.error("Error fetching jobs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch jobs", null));
        }
    }

    /**
     * ENDPOINT 10: Health check
     * GET /api/v1/admin/batch/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Batch service is healthy", null));
    }
}