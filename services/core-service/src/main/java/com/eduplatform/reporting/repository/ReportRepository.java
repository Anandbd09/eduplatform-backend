package com.eduplatform.reporting.repository;

import com.eduplatform.reporting.model.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends MongoRepository<Report, String> {

    /**
     * Find existing report to prevent duplicates
     */
    Optional<Report> findByReportedEntityIdAndReportedEntityTypeAndReporterIdAndTenantId(
            String entityId, String entityType, String reporterId, String tenantId);

    /**
     * Find reports by status
     */
    Page<Report> findByStatusAndTenantId(String status, String tenantId, Pageable pageable);

    /**
     * Find reports by reporter
     */
    Page<Report> findByReporterIdAndTenantId(String reporterId, String tenantId, Pageable pageable);

    /**
     * Find reports by category
     */
    Page<Report> findByCategoryAndTenantId(String category, String tenantId, Pageable pageable);

    /**
     * Find reports by severity
     */
    Page<Report> findBySeverityAndTenantId(String severity, String tenantId, Pageable pageable);

    /**
     * Find reports by tenant
     */
    Page<Report> findByTenantId(String tenantId, Pageable pageable);

    /**
     * Find reports for entity
     */
    Page<Report> findByReportedEntityIdAndTenantId(String entityId, String tenantId, Pageable pageable);

    /**
     * Find unresolved reports
     */
    @Query("{ 'status': { $nin: ['RESOLVED', 'DISMISSED'] }, 'tenantId': ?0 }")
    List<Report> findUnresolvedReports(String tenantId);

    /**
     * Find recent reports
     */
    List<Report> findByCreatedAtAfterAndTenantId(LocalDateTime date, String tenantId);

    /**
     * Count by status
     */
    Long countByStatusAndTenantId(String status, String tenantId);
}
