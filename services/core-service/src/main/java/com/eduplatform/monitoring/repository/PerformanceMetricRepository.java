// FILE 6: PerformanceMetricRepository.java
package com.eduplatform.monitoring.repository;

import com.eduplatform.monitoring.model.PerformanceMetric;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

@Repository
public interface PerformanceMetricRepository extends MongoRepository<PerformanceMetric, String> {

    Page<PerformanceMetric> findByEndpointAndTenantId(String endpoint, String tenantId, Pageable pageable);

    Page<PerformanceMetric> findByTimestampBetweenAndTenantId(LocalDateTime start, LocalDateTime end, String tenantId, Pageable pageable);
}