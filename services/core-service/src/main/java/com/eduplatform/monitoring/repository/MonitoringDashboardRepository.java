// FILE 8: MonitoringDashboardRepository.java
package com.eduplatform.monitoring.repository;

import com.eduplatform.monitoring.model.MonitoringDashboard;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MonitoringDashboardRepository extends MongoRepository<MonitoringDashboard, String> {

    Optional<MonitoringDashboard> findByDashboardNameAndTenantId(String name, String tenantId);
}