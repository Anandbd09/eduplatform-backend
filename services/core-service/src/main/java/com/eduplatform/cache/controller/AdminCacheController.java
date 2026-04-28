package com.eduplatform.cache.controller;

import com.eduplatform.cache.service.CachingService;
import com.eduplatform.cache.dto.*;
import com.eduplatform.cache.exception.CacheException;
//import com.eduplatform.common.response.ApiResponse;
import com.eduplatform.common.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/cache")
public class AdminCacheController {

    @Autowired
    private CachingService cachingService;

    /**
     * ENDPOINT 4: Invalidate cache entry
     * DELETE /api/v1/admin/cache/{cacheKey}
     */
    @DeleteMapping("/{cacheKey}")
    public ResponseEntity<?> invalidateCache(
            @PathVariable String cacheKey,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            cachingService.invalidateCache(cacheKey, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Cache entry invalidated", null));
        } catch (Exception e) {
            log.error("Error invalidating cache", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to invalidate cache", null));
        }
    }

    /**
     * ENDPOINT 5: Invalidate cache by type
     * DELETE /api/v1/admin/cache/type/{cacheType}
     */
    @DeleteMapping("/type/{cacheType}")
    public ResponseEntity<?> invalidateCacheByType(
            @PathVariable String cacheType,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            cachingService.invalidateCacheByType(cacheType, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Cache entries invalidated by type", null));
        } catch (Exception e) {
            log.error("Error invalidating cache by type", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to invalidate cache", null));
        }
    }

    /**
     * ENDPOINT 6: Cleanup expired entries
     * POST /api/v1/admin/cache/cleanup
     */
    @PostMapping("/cleanup")
    public ResponseEntity<?> cleanupExpiredEntries(
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            cachingService.cleanupExpiredEntries(tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Expired cache entries cleaned up", null));
        } catch (Exception e) {
            log.error("Error cleaning up expired entries", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to cleanup cache", null));
        }
    }
}