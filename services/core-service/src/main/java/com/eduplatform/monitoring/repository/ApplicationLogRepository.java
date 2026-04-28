// FILE 5: ApplicationLogRepository.java
package com.eduplatform.monitoring.repository;

import com.eduplatform.monitoring.model.ApplicationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ApplicationLogRepository extends MongoRepository<ApplicationLog, String> {

    Page<ApplicationLog> findByLevelAndTenantId(String level, String tenantId, Pageable pageable);

    Page<ApplicationLog> findByCategoryAndTenantId(String category, String tenantId, Pageable pageable);

    Page<ApplicationLog> findByTimestampBetweenAndTenantId(LocalDateTime start, LocalDateTime end, String tenantId, Pageable pageable);

    List<ApplicationLog> findByUserIdAndTenantIdOrderByTimestampDesc(String userId, String tenantId);
}