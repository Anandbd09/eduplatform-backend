package com.eduplatform.cache.service;

import com.eduplatform.cache.model.CacheEntry;
import com.eduplatform.cache.repository.CacheEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
public class CacheInvalidationService {

    @Autowired
    private CacheEntryRepository cacheEntryRepository;

    @Autowired
    private RedisService redisService;

    /**
     * INVALIDATE BY TRIGGER
     */
    public void invalidateByTrigger(String trigger, String tenantId) {
        try {
            // E.g., ORDER_PLACED → invalidate recommendation cache
            if ("ORDER_PLACED".equals(trigger)) {
                invalidateByType("RECOMMENDATION", tenantId);
            } else if ("USER_UPDATED".equals(trigger)) {
                invalidateByType("USER", tenantId);
            }

            log.info("Cache invalidated by trigger: {}", trigger);
        } catch (Exception e) {
            log.error("Error invalidating cache by trigger", e);
        }
    }

    /**
     * INVALIDATE BY TYPE
     */
    public void invalidateByType(String cacheType, String tenantId) {
        try {
            List<CacheEntry> entries = cacheEntryRepository
                    .findByCacheTypeAndTenantId(cacheType, tenantId,
                            org.springframework.data.domain.PageRequest.of(0, 10000))
                    .getContent();

            entries.forEach(e -> {
                e.setStatus("INVALID");
                cacheEntryRepository.save(e);
                redisService.delete(e.getCacheKey());
            });

            log.info("Invalidated {} cache entries of type: {}", entries.size(), cacheType);
        } catch (Exception e) {
            log.error("Error invalidating cache by type", e);
        }
    }
}