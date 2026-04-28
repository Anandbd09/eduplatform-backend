package com.eduplatform.reporting.service;

import com.eduplatform.reporting.model.*;
import com.eduplatform.reporting.repository.*;
import com.eduplatform.reporting.dto.*;
import com.eduplatform.reporting.exception.ReportingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class ReportingService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private DisputeService disputeService;

    @Autowired
    private NotificationService notificationService;

    /**
     * Create a new report
     */
    public ReportResponse createReport(ReportRequest request, String userId, String tenantId) {
        try {
            // Validate request
            request.validate();

            // Check for duplicates
            Optional<Report> existing = reportRepository.findByReportedEntityIdAndReportedEntityTypeAndReporterIdAndTenantId(
                    request.getReportedEntityId(),
                    request.getReportedEntityType(),
                    userId,
                    tenantId
            );

            if (existing.isPresent()) {
                throw new ReportingException("You have already reported this entity", "DUPLICATE_REPORT");
            }

            // Create report
            Report report = Report.builder()
                    .id(UUID.randomUUID().toString())
                    .reportedEntityId(request.getReportedEntityId())
                    .reportedEntityType(request.getReportedEntityType())
                    .reporterId(userId)
                    .reporterName(request.getReporterName())
                    .reporterEmail(request.getReporterEmail())
                    .category(request.getCategory())
                    .description(request.getDescription())
                    .severity(request.getSeverity())
                    .evidenceUrls(request.getEvidenceUrls())
                    .status("OPEN")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .tenantId(tenantId)
                    .version(0L)
                    .build();

            report.validate();
            Report saved = reportRepository.save(report);

            log.info("Report created: {} by user: {}", saved.getId(), userId);

            // Send notification to reporter
            notificationService.notifyReportCreated(saved);

            return convertToResponse(saved);

        } catch (ReportingException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating report", e);
            throw new ReportingException("Failed to create report");
        }
    }

    /**
     * Get report by ID
     */
    public ReportResponse getReport(String reportId, String tenantId) {
        try {
            Report report = reportRepository.findById(reportId)
                    .orElseThrow(() -> new ReportingException("Report not found", "REPORT_NOT_FOUND", 404));

            if (!report.getTenantId().equals(tenantId)) {
                throw new ReportingException("Unauthorized", "UNAUTHORIZED", 403);
            }

            return convertToResponse(report);

        } catch (ReportingException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching report", e);
            throw new ReportingException("Failed to fetch report");
        }
    }

    /**
     * Get all reports with filter
     */
    public Page<ReportResponse> getReports(int page, int size, String status, String category,
                                           String severity, String tenantId) {
        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

            Page<Report> reports;

            if (status != null && !status.isEmpty()) {
                reports = reportRepository.findByStatusAndTenantId(status, tenantId, pageable);
            } else if (category != null && !category.isEmpty()) {
                reports = reportRepository.findByCategoryAndTenantId(category, tenantId, pageable);
            } else if (severity != null && !severity.isEmpty()) {
                reports = reportRepository.findBySeverityAndTenantId(severity, tenantId, pageable);
            } else {
                reports = reportRepository.findByTenantId(tenantId, pageable);
            }

            return reports.map(this::convertToResponse);

        } catch (Exception e) {
            log.error("Error fetching reports", e);
            throw new ReportingException("Failed to fetch reports");
        }
    }

    /**
     * Get reports for reporter
     */
    public Page<ReportResponse> getMyReports(String reporterId, int page, int size, String tenantId) {
        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

            return reportRepository.findByReporterIdAndTenantId(reporterId, tenantId, pageable)
                    .map(this::convertToResponse);

        } catch (Exception e) {
            log.error("Error fetching user reports", e);
            throw new ReportingException("Failed to fetch reports");
        }
    }

    /**
     * Get reports for entity
     */
    public Page<ReportResponse> getEntityReports(String entityId, int page, int size, String tenantId) {
        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

            return reportRepository.findByReportedEntityIdAndTenantId(entityId, tenantId, pageable)
                    .map(this::convertToResponse);

        } catch (Exception e) {
            log.error("Error fetching entity reports", e);
            throw new ReportingException("Failed to fetch reports");
        }
    }

    /**
     * Update report status
     */
    public ReportResponse updateReportStatus(String reportId, String status, String tenantId) {
        try {
            Report report = reportRepository.findById(reportId)
                    .orElseThrow(() -> new ReportingException("Report not found", "REPORT_NOT_FOUND", 404));

            if (!report.getTenantId().equals(tenantId)) {
                throw new ReportingException("Unauthorized", "UNAUTHORIZED", 403);
            }

            report.setStatus(status);
            report.setUpdatedAt(LocalDateTime.now());

            if ("RESOLVED".equals(status) || "DISMISSED".equals(status)) {
                report.setResolvedAt(LocalDateTime.now());
            }

            Report saved = reportRepository.save(report);

            log.info("Report {} status updated to: {}", reportId, status);

            // Notify reporter of status change
            notificationService.notifyReportStatusChanged(saved);

            return convertToResponse(saved);

        } catch (ReportingException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating report status", e);
            throw new ReportingException("Failed to update report");
        }
    }

    /**
     * Get report statistics
     */
    public ReportingAnalytics getAnalytics(String tenantId) {
        try {
            Long openCount = reportRepository.countByStatusAndTenantId("OPEN", tenantId);
            Long reviewCount = reportRepository.countByStatusAndTenantId("UNDER_REVIEW", tenantId);
            Long resolvedCount = reportRepository.countByStatusAndTenantId("RESOLVED", tenantId);
            Long dismissedCount = reportRepository.countByStatusAndTenantId("DISMISSED", tenantId);

            Long totalReports = openCount + reviewCount + resolvedCount + dismissedCount;

            return ReportingAnalytics.builder()
                    .totalReports(totalReports.intValue())
                    .openReports(openCount.intValue())
                    .underReviewReports(reviewCount.intValue())
                    .resolvedReports(resolvedCount.intValue())
                    .dismissedReports(dismissedCount.intValue())
                    .resolutionRate(totalReports > 0 ? ((resolvedCount.doubleValue() / totalReports.doubleValue()) * 100) : 0.0)
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Error fetching analytics", e);
            throw new ReportingException("Failed to fetch analytics");
        }
    }

    /**
     * Convert to response
     */
    private ReportResponse convertToResponse(Report report) {
        return ReportResponse.builder()
                .id(report.getId())
                .reportedEntityId(report.getReportedEntityId())
                .reportedEntityType(report.getReportedEntityType())
                .reporterId(report.getReporterId())
                .reporterName(report.getReporterName())
                .category(report.getCategory())
                .description(report.getDescription())
                .severity(report.getSeverity())
                .status(report.getStatus())
                .evidenceCount(report.getEvidenceUrls() != null ? report.getEvidenceUrls().size() : 0)
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }
}
