package com.eduplatform.export.service;

import com.eduplatform.export.model.*;
import com.eduplatform.export.repository.*;
import com.eduplatform.export.dto.*;
import com.eduplatform.export.exception.ExportException;
import com.eduplatform.export.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@Transactional
public class ExportService {

    @Autowired
    private ExportJobRepository jobRepository;

    @Autowired
    private ExportTemplateRepository templateRepository;

    @Autowired
    private ExportAuditLogRepository auditRepository;

    @Autowired
    private CsvExportService csvService;

    @Autowired
    private PdfExportService pdfService;

    @Autowired
    private ExcelExportService excelService;

    /**
     * CREATE EXPORT JOB
     */
    public ExportJobResponse createExportJob(ExportJobRequest request, String userId, String tenantId) {
        try {
            // Validate request
            ExportValidator.validateExportRequest(request);

            String jobId = generateJobId();

            ExportJob job = ExportJob.builder()
                    .id(UUID.randomUUID().toString())
                    .jobId(jobId)
                    .userId(userId)
                    .exportType(request.getExportType())
                    .format(request.getFormat())
                    .sourceEntity(request.getSourceEntity())
                    .status("QUEUED")
                    .filterCriteria(request.getFilterCriteria())
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusDays(30))
                    .notificationEmail(request.getNotificationEmail())
                    .tenantId(tenantId)
                    .build();

            jobRepository.save(job);

            // Create audit log
            createAuditLog(jobId, userId, "CREATED", "Export job created", tenantId);

            // Start async processing
            processExportAsync(jobId, tenantId);

            log.info("Export job created: jobId={}, type={}, format={}", jobId, request.getExportType(), request.getFormat());

            return ExportJobResponse.builder()
                    .jobId(jobId)
                    .status("QUEUED")
                    .createdAt(job.getCreatedAt())
                    .build();

        } catch (ExportException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating export job", e);
            throw new ExportException("Failed to create export job");
        }
    }

    /**
     * PROCESS EXPORT ASYNC
     */
    @Async
    public void processExportAsync(String jobId, String tenantId) {
        try {
            Optional<ExportJob> job = jobRepository.findByJobIdAndTenantId(jobId, tenantId);

            if (job.isEmpty()) {
                throw new ExportException("Export job not found");
            }

            ExportJob exportJob = job.get();
            exportJob.setStatus("PROCESSING");
            exportJob.setStartedAt(LocalDateTime.now());
            jobRepository.save(exportJob);

            long startTime = System.currentTimeMillis();

            // Process based on format
            switch (exportJob.getFormat().toUpperCase()) {
                case "CSV":
                    csvService.exportToCsv(exportJob, tenantId);
                    break;
                case "PDF":
                    pdfService.exportToPdf(exportJob, tenantId);
                    break;
                case "EXCEL":
                    excelService.exportToExcel(exportJob, tenantId);
                    break;
                case "JSON":
                    exportToJson(exportJob, tenantId);
                    break;
                default:
                    throw new ExportException("Unsupported format: " + exportJob.getFormat());
            }

            long processingTime = System.currentTimeMillis() - startTime;

            exportJob.setStatus("COMPLETED");
            exportJob.setCompletedAt(LocalDateTime.now());
            exportJob.setProcessingTimeMs(processingTime);
            jobRepository.save(exportJob);

            createAuditLog(jobId, exportJob.getUserId(), "COMPLETED",
                    "Export completed in " + processingTime + "ms", tenantId);

            log.info("Export job completed: jobId={}, time={}ms", jobId, processingTime);

        } catch (Exception e) {
            log.error("Error processing export job", e);
            updateJobStatus(jobId, "FAILED", e.getMessage(), tenantId);
            createAuditLog(jobId, "SYSTEM", "FAILED", "Error: " + e.getMessage(), tenantId);
        }
    }

    /**
     * EXPORT TO JSON
     */
    private void exportToJson(ExportJob job, String tenantId) throws Exception {
        try {
            // Placeholder: In production, fetch actual data from services
            Map<String, Object> data = new HashMap<>();
            data.put("jobId", job.getJobId());
            data.put("exportType", job.getExportType());
            data.put("records", new ArrayList<>());

            // In production: save to storage, generate download URL
            String filePath = "/exports/" + job.getJobId() + ".json";
            job.setFilePath(filePath);
            job.setDownloadUrl("http://localhost:8081/api/v1/export/download/" + job.getJobId());

            log.debug("JSON export created: jobId={}", job.getJobId());

        } catch (Exception e) {
            throw new ExportException("Failed to export to JSON: " + e.getMessage());
        }
    }

    /**
     * GET EXPORT STATUS
     */
    public ExportStatusResponse getExportStatus(String jobId, String tenantId) {
        try {
            Optional<ExportJob> job = jobRepository.findByJobIdAndTenantId(jobId, tenantId);

            if (job.isEmpty()) {
                throw new ExportException("Export job not found");
            }

            ExportJob exportJob = job.get();

            return ExportStatusResponse.builder()
                    .jobId(exportJob.getJobId())
                    .status(exportJob.getStatus())
                    .totalRecords(exportJob.getTotalRecords())
                    .exportedRecords(exportJob.getExportedRecords())
                    .failedRecords(exportJob.getFailedRecords())
                    .createdAt(exportJob.getCreatedAt())
                    .completedAt(exportJob.getCompletedAt())
                    .processingTimeMs(exportJob.getProcessingTimeMs())
                    .downloadUrl(exportJob.getDownloadUrl())
                    .isExpired(!exportJob.isStillValid())
                    .build();

        } catch (ExportException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error getting export status", e);
            throw new ExportException("Failed to get export status");
        }
    }

    /**
     * DOWNLOAD EXPORT FILE
     */
    public ExportDownloadResponse downloadExport(String jobId, String tenantId) {
        try {
            Optional<ExportJob> job = jobRepository.findByJobIdAndTenantId(jobId, tenantId);

            if (job.isEmpty()) {
                throw new ExportException("Export job not found");
            }

            ExportJob exportJob = job.get();

            // Check if expired
            if (!exportJob.isStillValid()) {
                throw new ExportException("Export file has expired");
            }

            // Check if completed
            if (!"COMPLETED".equals(exportJob.getStatus())) {
                throw new ExportException("Export is still processing");
            }

            // Update last downloaded
            createAuditLog(jobId, exportJob.getUserId(), "DOWNLOADED",
                    "File downloaded", tenantId);

            log.info("Export downloaded: jobId={}", jobId);

            return ExportDownloadResponse.builder()
                    .fileName(exportJob.getJobId() + "." + getFileExtension(exportJob.getFormat()))
                    .fileSize(exportJob.getFileSize())
                    .mimeType(getMimeType(exportJob.getFormat()))
                    .filePath(exportJob.getFilePath())
                    .downloadUrl(exportJob.getDownloadUrl())
                    .build();

        } catch (ExportException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error downloading export", e);
            throw new ExportException("Failed to download export");
        }
    }

    /**
     * GET EXPORT HISTORY
     */
    public Page<ExportJobResponse> getExportHistory(String userId, int page, int size, String tenantId) {
        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

            Page<ExportJob> jobs = jobRepository.findByUserIdAndTenantId(userId, tenantId, pageable);

            return jobs.map(j -> ExportJobResponse.builder()
                    .jobId(j.getJobId())
                    .exportType(j.getExportType())
                    .format(j.getFormat())
                    .status(j.getStatus())
                    .createdAt(j.getCreatedAt())
                    .downloadUrl(j.getDownloadUrl())
                    .build());

        } catch (Exception e) {
            log.error("Error fetching export history", e);
            throw new ExportException("Failed to fetch export history");
        }
    }

    /**
     * CREATE EXPORT TEMPLATE
     */
    public ExportTemplateResponse createTemplate(ExportTemplateRequest request, String userId, String tenantId) {
        try {
            ExportTemplate template = ExportTemplate.builder()
                    .id(UUID.randomUUID().toString())
                    .templateName(request.getTemplateName())
                    .description(request.getDescription())
                    .sourceEntity(request.getSourceEntity())
                    .format(request.getFormat())
                    .selectedFields(request.getSelectedFields())
                    .filterCriteria(request.getFilterCriteria())
                    .isPublic(request.getIsPublic())
                    .createdBy(userId)
                    .createdAt(LocalDateTime.now())
                    .tenantId(tenantId)
                    .build();

            templateRepository.save(template);

            log.info("Export template created: templateId={}, name={}", template.getId(), template.getTemplateName());

            return ExportTemplateResponse.builder()
                    .id(template.getId())
                    .templateName(template.getTemplateName())
                    .format(template.getFormat())
                    .createdAt(template.getCreatedAt())
                    .build();

        } catch (Exception e) {
            log.error("Error creating export template", e);
            throw new ExportException("Failed to create export template");
        }
    }

    /**
     * GDPR DATA EXPORT
     */
    public ExportJobResponse gdprDataExport(String userId, String tenantId) {
        try {
            ExportJobRequest request = ExportJobRequest.builder()
                    .exportType("GDPR_DATA")
                    .format("JSON")
                    .sourceEntity("USER")
                    .build();

            ExportJob job = ExportJob.builder()
                    .id(UUID.randomUUID().toString())
                    .jobId(generateJobId())
                    .userId(userId)
                    .exportType("GDPR_DATA")
                    .format("JSON")
                    .sourceEntity("USER")
                    .status("QUEUED")
                    .isEncrypted(true)
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusDays(7)) // 7 days for GDPR
                    .tenantId(tenantId)
                    .build();

            jobRepository.save(job);

            createAuditLog(job.getJobId(), userId, "CREATED", "GDPR data export requested", tenantId);

            processExportAsync(job.getJobId(), tenantId);

            log.info("GDPR export initiated: userId={}, jobId={}", userId, job.getJobId());

            return ExportJobResponse.builder()
                    .jobId(job.getJobId())
                    .status("QUEUED")
                    .createdAt(job.getCreatedAt())
                    .build();

        } catch (Exception e) {
            log.error("Error initiating GDPR export", e);
            throw new ExportException("Failed to initiate GDPR data export");
        }
    }

    /**
     * DELETE EXPORT JOB
     */
    public void deleteExport(String jobId, String tenantId) {
        try {
            Optional<ExportJob> job = jobRepository.findByJobIdAndTenantId(jobId, tenantId);

            if (job.isPresent()) {
                jobRepository.delete(job.get());
                createAuditLog(jobId, "SYSTEM", "DELETED", "Export job deleted", tenantId);
                log.info("Export job deleted: jobId={}", jobId);
            }

        } catch (Exception e) {
            log.error("Error deleting export", e);
            throw new ExportException("Failed to delete export");
        }
    }

    /**
     * CLEANUP EXPIRED EXPORTS
     */
    public void cleanupExpiredExports(String tenantId) {
        try {
            List<ExportJob> expired = jobRepository
                    .findByExpiresAtBeforeAndTenantId(LocalDateTime.now(), tenantId);

            expired.forEach(job -> {
                jobRepository.delete(job);
                createAuditLog(job.getJobId(), "SYSTEM", "DELETED",
                        "Auto-deleted expired export", tenantId);
            });

            log.info("Cleaned up {} expired exports", expired.size());

        } catch (Exception e) {
            log.error("Error cleaning up expired exports", e);
        }
    }

    /**
     * HELPER: GENERATE JOB ID
     */
    private String generateJobId() {
        return "EXP-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * HELPER: UPDATE JOB STATUS
     */
    private void updateJobStatus(String jobId, String status, String errorMessage, String tenantId) {
        try {
            Optional<ExportJob> job = jobRepository.findByJobIdAndTenantId(jobId, tenantId);
            if (job.isPresent()) {
                ExportJob exportJob = job.get();
                exportJob.setStatus(status);
                exportJob.setErrorMessage(errorMessage);
                jobRepository.save(exportJob);
            }
        } catch (Exception e) {
            log.warn("Error updating job status", e);
        }
    }

    /**
     * HELPER: CREATE AUDIT LOG
     */
    private void createAuditLog(String jobId, String userId, String action, String details, String tenantId) {
        try {
            ExportAuditLog log = ExportAuditLog.builder()
                    .id(UUID.randomUUID().toString())
                    .jobId(jobId)
                    .userId(userId)
                    .action(action)
                    .details(details)
                    .timestamp(LocalDateTime.now())
                    .tenantId(tenantId)
                    .build();

            auditRepository.save(log);
        } catch (Exception e) {
            log.warn("Error creating audit log", e);
        }
    }

    /**
     * HELPER: GET FILE EXTENSION
     */
    private String getFileExtension(String format) {
        switch (format.toUpperCase()) {
            case "CSV":
                return "csv";
            case "PDF":
                return "pdf";
            case "EXCEL":
                return "xlsx";
            case "JSON":
                return "json";
            default:
                return "txt";
        }
    }

    /**
     * HELPER: GET MIME TYPE
     */
    private String getMimeType(String format) {
        switch (format.toUpperCase()) {
            case "CSV":
                return "text/csv";
            case "PDF":
                return "application/pdf";
            case "EXCEL":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "JSON":
                return "application/json";
            default:
                return "text/plain";
        }
    }
}