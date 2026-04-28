// FILE 5: CacheStatisticsRepository.java
package com.eduplatform.cache.repository;

import com.eduplatform.cache.model.CacheStatistics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CacheStatisticsRepository extends MongoRepository<CacheStatistics, String> {

    Optional<CacheStatistics> findByCacheTypeAndTenantId(String cacheType, String tenantId);
}