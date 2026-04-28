// FILE 7: ExportAuditLogRepository.java
package com.eduplatform.export.repository;

import com.eduplatform.export.model.ExportAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExportAuditLogRepository extends MongoRepository<ExportAuditLog, String> {

    Page<ExportAuditLog> findByJobIdAndTenantId(String jobId, String tenantId, Pageable pageable);

    Page<ExportAuditLog> findByUserIdAndTenantId(String userId, String tenantId, Pageable pageable);

    Page<ExportAuditLog> findByActionAndTenantId(String action, String tenantId, Pageable pageable);
}