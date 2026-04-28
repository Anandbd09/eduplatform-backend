// FILE 12: BatchAuditLogRepository.java
package com.eduplatform.batch.repository;

import com.eduplatform.batch.model.BatchAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BatchAuditLogRepository extends MongoRepository<BatchAuditLog, String> {

    Page<BatchAuditLog> findByJobIdAndTenantId(String jobId, String tenantId, Pageable pageable);
}