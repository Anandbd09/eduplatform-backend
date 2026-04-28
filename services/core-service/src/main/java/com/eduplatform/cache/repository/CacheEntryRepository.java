// FILE 4: CacheEntryRepository.java
package com.eduplatform.cache.repository;

import com.eduplatform.cache.model.CacheEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CacheEntryRepository extends MongoRepository<CacheEntry, String> {

    Optional<CacheEntry> findByCacheKeyAndTenantId(String cacheKey, String tenantId);

    Page<CacheEntry> findByCacheTypeAndTenantId(String cacheType, String tenantId, Pageable pageable);

    Page<CacheEntry> findByStatusAndTenantId(String status, String tenantId, Pageable pageable);

    List<CacheEntry> findByExpiresAtBeforeAndTenantId(LocalDateTime before, String tenantId);

    Long countByCacheTypeAndStatusAndTenantId(String cacheType, String status, String tenantId);

    void deleteByCacheKeyAndTenantId(String cacheKey, String tenantId);
}