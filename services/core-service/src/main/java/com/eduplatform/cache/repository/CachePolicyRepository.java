// FILE 6: CachePolicyRepository.java
package com.eduplatform.cache.repository;

import com.eduplatform.cache.model.CachePolicy;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CachePolicyRepository extends MongoRepository<CachePolicy, String> {

    Optional<CachePolicy> findByCacheTypeAndTenantId(String cacheType, String tenantId);
}