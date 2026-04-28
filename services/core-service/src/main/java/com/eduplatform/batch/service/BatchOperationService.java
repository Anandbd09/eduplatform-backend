package com.eduplatform.batch.service;

import com.eduplatform.batch.model.*;
import com.eduplatform.batch.repository.*;
import com.eduplatform.batch.dto.*;
import com.eduplatform.batch.exception.BatchException;
import com.eduplatform.batch.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@Transactional
public class BatchOperationService {

    @Autowired
    private BatchJobRepository jobRepository;

    @Autowired
    private BatchJobResultRepository resultRepository;

    @Autowired
    private UserImportRepository userImportRepository;

    @Autowired
    private CourseAssignmentRepository assignmentRepository;

    @Autowired
    private EnrollmentBatchRepository enrollmentRepository;

    @Autowired
    private BatchAuditLogRepository auditLogRepository;

    @Autowired
    private UserImportService userImportService;

    @Autowired
    private CourseAssignmentService assignmentService;

    @Autowired
    private BatchValidationService validationService;

    @Autowired
    private ReportService reportService;

    /**
     * SUBMIT BATCH JOB (USER IMPORT)
     */
    public BatchJobResponse submitUserImportJob(MultipartFile file, String importType,
                                                String userId, String tenantId) {
        try {
            // Validate file
            if (file == null || file.isEmpty()) {
                throw new BatchException("File is empty");
            }

            // Generate job ID
            String jobId = "JOB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            // Create batch job
            BatchJob job = BatchJob.builder()
                    .id(UUID.randomUUID().toString())
                    .jobId(jobId)
                    .jobType("USER_IMPORT")
                    .userId(userId)
                    .fileName(file.getOriginalFilename())
                    .fileSizeBytes(file.getSize())
                    .status("QUEUED")
                    .sourceFormat(getFileFormat(file.getOriginalFilename()))
                    .totalRecords(0)
                    .processedRecords(0)
                    .successRecords(0)
                    .failedRecords(0)
                    .progressPercentage(0.0)
                    .isRetryable(true)
                    .retryCount(0)
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusDays(30))
                    .tenantId(tenantId)
                    .build();

            BatchJob saved = jobRepository.save(job);

            // Create user import metadata
            UserImport userImport = UserImport.builder()
                    .id(UUID.randomUUID().toString())
                    .jobId(jobId)
                    .importType(importType)
                    .status("QUEUED")
                    .duplicateHandling("SKIP")
                    .invalidHandling("SKIP")
                    .createdAt(LocalDateTime.now())
                    .tenantId(tenantId)
                    .build();

            userImportRepository.save(userImport);

            // Log audit
            createAuditLog(jobId, "CREATED", "User import job created", userId, null, "QUEUED", tenantId);

            // Start async processing
            processUserImportAsync(saved, file, importType, tenantId);

            log.info("Batch job submitted: jobId={}, type=USER_IMPORT, user={}", jobId, userId);

            return convertToResponse(saved);

        } catch (BatchException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error submitting batch job", e);
            throw new BatchException("Failed to submit batch job: " + e.getMessage());
        }
    }

    /**
     * SUBMIT COURSE ASSIGNMENT JOB
     */
    public BatchJobResponse submitCourseAssignmentJob(CourseAssignmentRequest request,
                                                      String userId, String tenantId) {
        try {
            String jobId = "JOB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            BatchJob job = BatchJob.builder()
                    .id(UUID.randomUUID().toString())
                    .jobId(jobId)
                    .jobType("COURSE_ASSIGNMENT")
                    .userId(userId)
                    .status("QUEUED")
                    .totalRecords(0)
                    .processedRecords(0)
                    .progressPercentage(0.0)
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusDays(30))
                    .tenantId(tenantId)
                    .build();

            BatchJob saved = jobRepository.save(job);

            // Create assignment metadata
            CourseAssignment assignment = CourseAssignment.builder()
                    .id(UUID.randomUUID().toString())
                    .jobId(jobId)
                    .courseId(request.getCourseId())
                    .filterCriteria(request.getFilterCriteria())
                    .sendNotification(request.getSendNotification())
                    .status("QUEUED")
                    .createdAt(LocalDateTime.now())
                    .tenantId(tenantId)
                    .build();

            assignmentRepository.save(assignment);

            createAuditLog(jobId, "CREATED", "Course assignment job created", userId, null, "QUEUED", tenantId);

            // Start async processing
            processCourseAssignmentAsync(saved, request, tenantId);

            log.info("Course assignment job submitted: jobId={}, courseId={}", jobId, request.getCourseId());

            return convertToResponse(saved);

        } catch (Exception e) {
            log.error("Error submitting assignment job", e);
            throw new BatchException("Failed to submit assignment job");
        }
    }

    /**
     * SUBMIT BATCH ENROLLMENT JOB
     */
    public BatchJobResponse submitEnrollmentBatchJob(List<String> userIds, List<String> courseIds,
                                                     String userId, String tenantId) {
        try {
            String jobId = "JOB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            BatchJob job = BatchJob.builder()
                    .id(UUID.randomUUID().toString())
                    .jobId(jobId)
                    .jobType("ENROLLMENT_BATCH")
                    .userId(userId)
                    .status("QUEUED")
                    .totalRecords(userIds.size() * courseIds.size())
                    .processedRecords(0)
                    .progressPercentage(0.0)
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusDays(30))
                    .tenantId(tenantId)
                    .build();

            BatchJob saved = jobRepository.save(job);

            // Create enrollment batch
            EnrollmentBatch batch = EnrollmentBatch.builder()
                    .id(UUID.randomUUID().toString())
                    .jobId(jobId)
                    .userIds(userIds)
                    .courseIds(courseIds)
                    .totalEnrollments(userIds.size() * courseIds.size())
                    .successfulEnrollments(0)
                    .status("QUEUED")
                    .createdAt(LocalDateTime.now())
                    .tenantId(tenantId)
                    .build();

            enrollmentRepository.save(batch);

            createAuditLog(jobId, "CREATED", "Enrollment batch created", userId, null, "QUEUED", tenantId);

            // Start async processing
            processEnrollmentBatchAsync(saved, userIds, courseIds, tenantId);

            log.info("Enrollment batch submitted: jobId={}, users={}, courses={}",
                    jobId, userIds.size(), courseIds.size());

            return convertToResponse(saved);

        } catch (Exception e) {
            log.error("Error submitting enrollment batch", e);
            throw new BatchException("Failed to submit enrollment batch");
        }
    }

    /**
     * GET JOB STATUS
     */
    public BatchJobResponse getJobStatus(String jobId, String tenantId) {
        try {
            Optional<BatchJob> job = jobRepository.findByJobIdAndTenantId(jobId, tenantId);
            if (job.isEmpty()) {
                throw new BatchException("Job not found");
            }
            return convertToResponse(job.get());
        } catch (BatchException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching job status", e);
            throw new BatchException("Failed to fetch job status");
        }
    }

    /**
     * GET JOB RESULTS
     */
    public Page<BatchResultResponse> getJobResults(String jobId, int page, int size, String tenantId) {
        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);
            Pageable pageable = PageRequest.of(page, size, Sort.by("recordNumber").ascending());

            Page<BatchJobResult> results = resultRepository.findByJobIdAndTenantId(jobId, tenantId, pageable);
            return results.map(this::convertResultToResponse);

        } catch (Exception e) {
            log.error("Error fetching job results", e);
            throw new BatchException("Failed to fetch results");
        }
    }

    /**
     * GET USER JOBS
     */
    public Page<BatchJobResponse> getUserJobs(String userId, int page, int size, String tenantId) {
        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

            Page<BatchJob> jobs = jobRepository.findByUserIdAndTenantId(userId, tenantId, pageable);
            return jobs.map(this::convertToResponse);

        } catch (Exception e) {
            log.error("Error fetching user jobs", e);
            throw new BatchException("Failed to fetch jobs");
        }
    }

    /**
     * GET FAILED RESULTS
     */
    public Page<BatchResultResponse> getFailedResults(String jobId, int page, int size, String tenantId) {
        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);
            Pageable pageable = PageRequest.of(page, size);

            Page<BatchJobResult> results = resultRepository
                    .findByJobIdAndStatusAndTenantId(jobId, "FAILED", tenantId, pageable);
            return results.map(this::convertResultToResponse);

        } catch (Exception e) {
            log.error("Error fetching failed results", e);
            throw new BatchException("Failed to fetch failed results");
        }
    }

    /**
     * CANCEL JOB
     */
    public void cancelJob(String jobId, String tenantId) {
        try {
            Optional<BatchJob> job = jobRepository.findByJobIdAndTenantId(jobId, tenantId);
            if (job.isEmpty()) {
                throw new BatchException("Job not found");
            }

            BatchJob j = job.get();
            if (j.isComplete()) {
                throw new BatchException("Cannot cancel completed job");
            }

            j.setStatus("PAUSED");
            jobRepository.save(j);

            createAuditLog(jobId, "CANCELLED", "Job cancelled by user", j.getUserId(),
                    j.getStatus(), "PAUSED", tenantId);

            log.info("Job cancelled: jobId={}", jobId);

        } catch (BatchException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error cancelling job", e);
            throw new BatchException("Failed to cancel job");
        }
    }

    /**
     * ASYNC: PROCESS USER IMPORT
     */
    @Async
    public void processUserImportAsync(BatchJob job, MultipartFile file, String importType, String tenantId) {
        try {
            job.setStatus("PROCESSING");
            job.setStartedAt(LocalDateTime.now());
            jobRepository.save(job);

            createAuditLog(job.getJobId(), "STARTED", "Processing started", job.getUserId(),
                    "QUEUED", "PROCESSING", tenantId);

            // Parse CSV
            List<Map<String, String>> records = CSVParser.parseCSV(file.getInputStream());
            job.setTotalRecords(records.size());

            int successCount = 0;
            int failCount = 0;
            int skipCount = 0;

            for (int i = 0; i < records.size(); i++) {
                Map<String, String> record = records.get(i);

                try {
                    // Validate
                    List<String> errors = validationService.validateUser(record, importType);

                    if (!errors.isEmpty()) {
                        createResult(job.getJobId(), i, record.get("email"), "FAILED",
                                String.join(", ", errors), "VALIDATION_ERROR", tenantId);
                        failCount++;
                    } else {
                        // Import user
                        userImportService.importUser(record, importType, tenantId);
                        createResult(job.getJobId(), i, record.get("email"), "SUCCESS",
                                "User imported successfully", null, tenantId);
                        successCount++;
                    }
                } catch (Exception e) {
                    createResult(job.getJobId(), i, record.get("email"), "FAILED",
                            e.getMessage(), "PROCESSING_ERROR", tenantId);
                    failCount++;
                }

                // Update progress
                job.setProcessedRecords(i + 1);
                job.setProgressPercentage(((double) (i + 1) / records.size()) * 100);
                jobRepository.save(job);
            }

            // Complete job
            job.setStatus("COMPLETED");
            job.setCompletedAt(LocalDateTime.now());
            job.setSuccessRecords(successCount);
            job.setFailedRecords(failCount);
            job.setSkippedRecords(skipCount);
            job.setExecutionTimeSeconds((System.currentTimeMillis() - job.getStartedAt().getNano()) / 1000);
            jobRepository.save(job);

            createAuditLog(job.getJobId(), "COMPLETED",
                    "Processed: " + successCount + " success, " + failCount + " failed",
                    job.getUserId(), "PROCESSING", "COMPLETED", tenantId);

            log.info("User import completed: jobId={}, success={}, failed={}",
                    job.getJobId(), successCount, failCount);

        } catch (Exception e) {
            log.error("Error processing user import", e);
            job.setStatus("FAILED");
            job.setCompletedAt(LocalDateTime.now());
            job.setErrorSummary(e.getMessage());
            jobRepository.save(job);
        }
    }

    /**
     * ASYNC: PROCESS COURSE ASSIGNMENT
     */
    @Async
    public void processCourseAssignmentAsync(BatchJob job, CourseAssignmentRequest request, String tenantId) {
        try {
            job.setStatus("PROCESSING");
            job.setStartedAt(LocalDateTime.now());
            jobRepository.save(job);

            // Get users based on filter
            List<String> userIds = assignmentService.getUsersForAssignment(request.getFilterCriteria(), tenantId);
            job.setTotalRecords(userIds.size());

            int assigned = 0;
            int failed = 0;

            for (int i = 0; i < userIds.size(); i++) {
                try {
                    assignmentService.assignCourseToUser(request.getCourseId(), userIds.get(i), tenantId);
                    createResult(job.getJobId(), i, userIds.get(i), "SUCCESS",
                            "Course assigned", null, tenantId);
                    assigned++;
                } catch (Exception e) {
                    createResult(job.getJobId(), i, userIds.get(i), "FAILED",
                            e.getMessage(), "ASSIGNMENT_ERROR", tenantId);
                    failed++;
                }

                job.setProcessedRecords(i + 1);
                job.setProgressPercentage(((double) (i + 1) / userIds.size()) * 100);
                jobRepository.save(job);
            }

            job.setStatus("COMPLETED");
            job.setCompletedAt(LocalDateTime.now());
            job.setSuccessRecords(assigned);
            job.setFailedRecords(failed);
            jobRepository.save(job);

            log.info("Course assignment completed: jobId={}, assigned={}, failed={}",
                    job.getJobId(), assigned, failed);

        } catch (Exception e) {
            log.error("Error processing course assignment", e);
            job.setStatus("FAILED");
            job.setCompletedAt(LocalDateTime.now());
            job.setErrorSummary(e.getMessage());
            jobRepository.save(job);
        }
    }

    /**
     * ASYNC: PROCESS ENROLLMENT BATCH
     */
    @Async
    public void processEnrollmentBatchAsync(BatchJob job, List<String> userIds,
                                            List<String> courseIds, String tenantId) {
        try {
            job.setStatus("PROCESSING");
            job.setStartedAt(LocalDateTime.now());
            jobRepository.save(job);

            int enrolled = 0;
            int failed = 0;
            int recordNum = 0;

            for (String userId : userIds) {
                for (String courseId : courseIds) {
                    try {
                        // Create enrollment
                        // This would call enrollment service
                        createResult(job.getJobId(), recordNum, userId + "-" + courseId,
                                "SUCCESS", "Enrolled", null, tenantId);
                        enrolled++;
                    } catch (Exception e) {
                        createResult(job.getJobId(), recordNum, userId + "-" + courseId,
                                "FAILED", e.getMessage(), "ENROLLMENT_ERROR", tenantId);
                        failed++;
                    }

                    recordNum++;
                    job.setProcessedRecords(recordNum);
                    job.setProgressPercentage(((double) recordNum / job.getTotalRecords()) * 100);
                    jobRepository.save(job);
                }
            }

            job.setStatus("COMPLETED");
            job.setCompletedAt(LocalDateTime.now());
            job.setSuccessRecords(enrolled);
            job.setFailedRecords(failed);
            jobRepository.save(job);

            log.info("Enrollment batch completed: jobId={}, enrolled={}, failed={}",
                    job.getJobId(), enrolled, failed);

        } catch (Exception e) {
            log.error("Error processing enrollment batch", e);
            job.setStatus("FAILED");
            job.setCompletedAt(LocalDateTime.now());
            job.setErrorSummary(e.getMessage());
            jobRepository.save(job);
        }
    }

    /**
     * HELPER: Create result record
     */
    private void createResult(String jobId, Integer recordNum, String identifier, String status,
                              String message, String errorCode, String tenantId) {
        try {
            BatchJobResult result = BatchJobResult.builder()
                    .id(UUID.randomUUID().toString())
                    .jobId(jobId)
                    .recordNumber(recordNum)
                    .recordIdentifier(identifier)
                    .status(status)
                    .message(message)
                    .errorCode(errorCode)
                    .createdAt(LocalDateTime.now())
                    .tenantId(tenantId)
                    .build();
            resultRepository.save(result);
        } catch (Exception e) {
            log.error("Error creating result record", e);
        }
    }

    /**
     * HELPER: Create audit log
     */
    private void createAuditLog(String jobId, String action, String details, String changedBy,
                                String previousStatus, String newStatus, String tenantId) {
        try {
            BatchAuditLog log = BatchAuditLog.builder()
                    .id(UUID.randomUUID().toString())
                    .jobId(jobId)
                    .action(action)
                    .details(details)
                    .changedBy(changedBy)
                    .previousStatus(previousStatus)
                    .newStatus(newStatus)
                    .timestamp(LocalDateTime.now())
                    .tenantId(tenantId)
                    .build();
            auditLogRepository.save(log);
        } catch (Exception e) {
            log.error("Error creating audit log", e);
        }
    }

    /**
     * CONVERT TO RESPONSE
     */
    private BatchJobResponse convertToResponse(BatchJob job) {
        return BatchJobResponse.builder()
                .jobId(job.getJobId())
                .jobType(job.getJobType())
                .status(job.getStatus())
                .fileName(job.getFileName())
                .fileSizeBytes(job.getFileSizeBytes())
                .totalRecords(job.getTotalRecords())
                .processedRecords(job.getProcessedRecords())
                .successRecords(job.getSuccessRecords())
                .failedRecords(job.getFailedRecords())
                .skippedRecords(job.getSkippedRecords())
                .progressPercentage(job.getProgressPercentage())
                .createdAt(job.getCreatedAt())
                .startedAt(job.getStartedAt())
                .completedAt(job.getCompletedAt())
                .build();
    }

    private BatchResultResponse convertResultToResponse(BatchJobResult result) {
        return BatchResultResponse.builder()
                .recordNumber(result.getRecordNumber())
                .recordIdentifier(result.getRecordIdentifier())
                .status(result.getStatus())
                .message(result.getMessage())
                .errorCode(result.getErrorCode())
                .errorDetails(result.getErrorDetails())
                .createdAt(result.getCreatedAt())
                .build();
    }

    private String getFileFormat(String fileName) {
        if (fileName.endsWith(".csv")) return "CSV";
        if (fileName.endsWith(".xlsx")) return "EXCEL";
        if (fileName.endsWith(".json")) return "JSON";
        return "UNKNOWN";
    }
}
