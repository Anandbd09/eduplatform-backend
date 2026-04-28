package com.eduplatform.cache.service;

import com.eduplatform.cache.model.CacheStatistics;
import com.eduplatform.cache.repository.CacheStatisticsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
public class CacheAnalyticsService {

    @Autowired
    private CacheStatisticsRepository statisticsRepository;

    /**
     * RECORD CACHE HIT
     */
    public void recordHit(String cacheKey, String tenantId) {
        try {
            // Parse cacheType from cacheKey (format: "TYPE:ID")
            String cacheType = cacheKey.split(":")[0];

            // Update or create statistics
            var stats = statisticsRepository.findByCacheTypeAndTenantId(cacheType, tenantId);

            if (stats.isPresent()) {
                CacheStatistics s = stats.get();
                s.setTotalHits((s.getTotalHits() != null ? s.getTotalHits() : 0) + 1);
                s.setHitRate(calculateHitRate(s.getTotalHits(), s.getTotalMisses()));
                s.setLastUpdatedAt(LocalDateTime.now());
                statisticsRepository.save(s);
            }
        } catch (Exception e) {
            log.warn("Error recording cache hit", e);
        }
    }

    /**
     * RECORD CACHE MISS
     */
    public void recordMiss(String cacheKey, String tenantId) {
        try {
            String cacheType = cacheKey.split(":")[0];

            var stats = statisticsRepository.findByCacheTypeAndTenantId(cacheType, tenantId);

            if (stats.isPresent()) {
                CacheStatistics s = stats.get();
                s.setTotalMisses((s.getTotalMisses() != null ? s.getTotalMisses() : 0) + 1);
                s.setHitRate(calculateHitRate(s.getTotalHits(), s.getTotalMisses()));
                s.setLastUpdatedAt(LocalDateTime.now());
                statisticsRepository.save(s);
            }
        } catch (Exception e) {
            log.warn("Error recording cache miss", e);
        }
    }

    /**
     * CALCULATE HIT RATE
     */
    private Double calculateHitRate(Long hits, Long misses) {
        long total = (hits != null ? hits : 0) + (misses != null ? misses : 0);
        if (total == 0) return 0.0;
        return ((double) (hits != null ? hits : 0) / total) * 100;
    }
}