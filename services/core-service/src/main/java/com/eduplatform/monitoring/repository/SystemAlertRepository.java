// FILE 7: SystemAlertRepository.java
package com.eduplatform.monitoring.repository;

import com.eduplatform.monitoring.model.SystemAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SystemAlertRepository extends MongoRepository<SystemAlert, String> {

    Page<SystemAlert> findByStatusAndTenantId(String status, String tenantId, Pageable pageable);

    Page<SystemAlert> findBySeverityAndTenantId(String severity, String tenantId, Pageable pageable);

    List<SystemAlert> findByStatusAndTenantIdOrderByCreatedAtDesc(String status, String tenantId);
}