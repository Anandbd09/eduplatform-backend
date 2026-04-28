// FILE 8: SecurityAuditLogRepository.java
package com.eduplatform.security.repository;

import com.eduplatform.security.model.SecurityAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityAuditLogRepository extends MongoRepository<SecurityAuditLog, String> {

    Page<SecurityAuditLog> findByUserIdAndTenantId(String userId, String tenantId, Pageable pageable);

    Page<SecurityAuditLog> findByActionAndTenantId(String action, String tenantId, Pageable pageable);
}