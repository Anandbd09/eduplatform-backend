package com.eduplatform.cache.service;

import com.eduplatform.cache.model.*;
import com.eduplatform.cache.repository.*;
import com.eduplatform.cache.dto.*;
import com.eduplatform.cache.exception.CacheException;
import com.eduplatform.cache.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@Transactional
public class CachingService {

    @Autowired
    private CacheEntryRepository cacheEntryRepository;

    @Autowired
    private CacheStatisticsRepository statisticsRepository;

    @Autowired
    private CachePolicyRepository policyRepository;

    @Autowired
    private RedisService redisService;

    @Autowired
    private CacheInvalidationService invalidationService;

    @Autowired
    private CacheAnalyticsService analyticsService;

    /**
     * GET FROM CACHE (WITH REDIS)
     */
    @Cacheable(value = "eduplatform", key = "#cacheKey", unless = "#result == null")
    public Object getCachedValue(String cacheKey, String tenantId) {
        try {
            // Try Redis first (hot cache)
            Object redisValue = redisService.get(cacheKey);
            if (redisValue != null) {
                recordCacheHit(cacheKey, tenantId);
                return redisValue;
            }

            // Fall back to MongoDB (cold cache)
            Optional<CacheEntry> entry = cacheEntryRepository.findByCacheKeyAndTenantId(cacheKey, tenantId);

            if (entry.isPresent() && entry.get().isStillValid()) {
                CacheEntry e = entry.get();
                e.setAccessCount((e.getAccessCount() != null ? e.getAccessCount() : 0) + 1);
                e.setLastAccessedAt(LocalDateTime.now());
                cacheEntryRepository.save(e);

                // Move to Redis for next access (hot cache promotion)
                redisService.set(cacheKey, e.getCachedValue(), e.getTtlSeconds());

                recordCacheHit(cacheKey, tenantId);
                return e.getCachedValue();
            }

            recordCacheMiss(cacheKey, tenantId);
            return null;

        } catch (Exception e) {
            log.error("Error getting cached value", e);
            recordCacheMiss(cacheKey, tenantId);
            return null;
        }
    }

    /**
     * PUT IN CACHE
     */
    public void putInCache(String cacheKey, Object value, String cacheType,
                           Integer ttlSeconds, String tenantId) {
        try {
            // Get policy for this cache type
            Optional<CachePolicy> policy = policyRepository.findByCacheTypeAndTenantId(cacheType, tenantId);

            Integer finalTtl = ttlSeconds;
            if (policy.isPresent()) {
                if (ttlSeconds == null) {
                    finalTtl = policy.get().getDefaultTtlSeconds();
                } else if (ttlSeconds > policy.get().getMaxTtlSeconds()) {
                    finalTtl = policy.get().getMaxTtlSeconds();
                }
            } else {
                finalTtl = 3600; // Default 1 hour
            }

            // Create cache entry
            String valueHash = CacheKeyGenerator.generateHash(value);
            CacheEntry entry = CacheEntry.builder()
                    .id(UUID.randomUUID().toString())
                    .cacheKey(cacheKey)
                    .cacheType(cacheType)
                    .cachedValue(value)
                    .valueHash(valueHash)
                    .status("ACTIVE")
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusSeconds(finalTtl))
                    .accessCount(1L)
                    .ttlSeconds(finalTtl)
                    .isHot(policy.isPresent() && policy.get().getIsHotData())
                    .tenantId(tenantId)
                    .build();

            // Save to MongoDB
            cacheEntryRepository.save(entry);

            // Save to Redis (hot cache)
            redisService.set(cacheKey, value, finalTtl);

            log.info("Cache entry saved: key={}, type={}, ttl={}", cacheKey, cacheType, finalTtl);

        } catch (Exception e) {
            log.error("Error putting value in cache", e);
            throw new CacheException("Failed to cache value");
        }
    }

    /**
     * INVALIDATE CACHE
     */
    public void invalidateCache(String cacheKey, String tenantId) {
        try {
            Optional<CacheEntry> entry = cacheEntryRepository.findByCacheKeyAndTenantId(cacheKey, tenantId);

            if (entry.isPresent()) {
                CacheEntry e = entry.get();
                e.setStatus("INVALID");
                cacheEntryRepository.save(e);
            }

            // Also delete from Redis
            redisService.delete(cacheKey);

            log.info("Cache invalidated: key={}", cacheKey);

        } catch (Exception e) {
            log.error("Error invalidating cache", e);
        }
    }

    /**
     * INVALIDATE BY TYPE
     */
    public void invalidateCacheByType(String cacheType, String tenantId) {
        try {
            Page<CacheEntry> entries = cacheEntryRepository
                    .findByCacheTypeAndTenantId(cacheType, tenantId, PageRequest.of(0, 10000));

            entries.getContent().forEach(e -> {
                e.setStatus("INVALID");
                cacheEntryRepository.save(e);
                redisService.delete(e.getCacheKey());
            });

            log.info("Cache invalidated by type: type={}, count={}", cacheType, entries.getTotalElements());

        } catch (Exception e) {
            log.error("Error invalidating cache by type", e);
        }
    }

    /**
     * CLEANUP EXPIRED ENTRIES
     */
    @Transactional
    public void cleanupExpiredEntries(String tenantId) {
        try {
            List<CacheEntry> expired = cacheEntryRepository
                    .findByExpiresAtBeforeAndTenantId(LocalDateTime.now(), tenantId);

            expired.forEach(e -> {
                e.setStatus("EXPIRED");
                cacheEntryRepository.save(e);
                redisService.delete(e.getCacheKey());
            });

            log.info("Cleaned up {} expired cache entries", expired.size());

        } catch (Exception e) {
            log.error("Error cleaning up expired entries", e);
        }
    }

    /**
     * EVICT LEAST USED ENTRIES (WHEN MEMORY FULL)
     */
    public void evictLeastUsedEntries(String cacheType, String tenantId) {
        try {
            Optional<CachePolicy> policy = policyRepository.findByCacheTypeAndTenantId(cacheType, tenantId);
            if (policy.isEmpty()) return;

            String evictionPolicy = policy.get().getEvictionPolicy();

            Page<CacheEntry> entries = cacheEntryRepository
                    .findByCacheTypeAndTenantId(cacheType, tenantId,
                            PageRequest.of(0, 1000, Sort.by("accessCount").ascending()));

            // Evict first 10% of entries (lowest access count)
            int evictCount = (int) (entries.getTotalElements() * 0.1);
            entries.stream().limit(evictCount).forEach(e -> {
                e.setStatus("EVICTED");
                cacheEntryRepository.save(e);
                redisService.delete(e.getCacheKey());
            });

            log.info("Evicted {} entries: policy={}, type={}", evictCount, evictionPolicy, cacheType);

        } catch (Exception e) {
            log.error("Error evicting entries", e);
        }
    }

    /**
     * GET CACHE STATISTICS
     */
    public CacheStatisticsResponse getCacheStatistics(String cacheType, String tenantId) {
        try {
            Optional<CacheStatistics> stats = statisticsRepository
                    .findByCacheTypeAndTenantId(cacheType, tenantId);

            if (stats.isEmpty()) {
                // Calculate on the fly
                Long total = cacheEntryRepository
                        .countByCacheTypeAndStatusAndTenantId(cacheType, "ACTIVE", tenantId);

                return CacheStatisticsResponse.builder()
                        .cacheType(cacheType)
                        .totalEntries(total)
                        .hitRate(0.0)
                        .memoryUsedMb(0L)
                        .build();
            }

            CacheStatistics s = stats.get();
            return CacheStatisticsResponse.builder()
                    .cacheType(s.getCacheType())
                    .totalEntries(s.getTotalEntries())
                    .activeEntries(s.getActiveEntries())
                    .hitRate(s.getHitRatio())
                    .memoryUsedMb(s.getTotalMemoryUsedBytes() / (1024 * 1024))
                    .build();

        } catch (Exception e) {
            log.error("Error getting cache statistics", e);
            throw new CacheException("Failed to get statistics");
        }
    }

    /**
     * GET CACHED ENTRIES
     */
    public Page<CacheEntryResponse> getCachedEntries(String cacheType, int page, int size, String tenantId) {
        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);
            Pageable pageable = PageRequest.of(page, size, Sort.by("lastAccessedAt").descending());

            Page<CacheEntry> entries = cacheEntryRepository
                    .findByCacheTypeAndTenantId(cacheType, tenantId, pageable);

            return entries.map(e -> CacheEntryResponse.builder()
                    .cacheKey(e.getCacheKey())
                    .cacheType(e.getCacheType())
                    .status(e.getStatus())
                    .accessCount(e.getAccessCount())
                    .ttlSeconds(e.getTtlSeconds())
                    .createdAt(e.getCreatedAt())
                    .expiresAt(e.getExpiresAt())
                    .build());

        } catch (Exception e) {
            log.error("Error fetching cached entries", e);
            throw new CacheException("Failed to fetch entries");
        }
    }

    /**
     * RECORD CACHE HIT
     */
    private void recordCacheHit(String cacheKey, String tenantId) {
        try {
            analyticsService.recordHit(cacheKey, tenantId);
        } catch (Exception e) {
            log.warn("Error recording cache hit", e);
        }
    }

    /**
     * RECORD CACHE MISS
     */
    private void recordCacheMiss(String cacheKey, String tenantId) {
        try {
            analyticsService.recordMiss(cacheKey, tenantId);
        } catch (Exception e) {
            log.warn("Error recording cache miss", e);
        }
    }
}
