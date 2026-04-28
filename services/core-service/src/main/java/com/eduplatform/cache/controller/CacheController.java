package com.eduplatform.cache.controller;

import com.eduplatform.cache.service.CachingService;
import com.eduplatform.cache.dto.*;
import com.eduplatform.cache.exception.CacheException;
//import com.eduplatform.common.response.ApiResponse;
import com.eduplatform.common.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/cache")
public class CacheController {

    @Autowired
    private CachingService cachingService;

    /**
     * ENDPOINT 1: Get cached value
     * GET /api/v1/cache/{cacheKey}
     */
    @GetMapping("/{cacheKey}")
    public ResponseEntity<?> getCachedValue(
            @PathVariable String cacheKey,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            Object value = cachingService.getCachedValue(cacheKey, tenantId);

            if (value == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Cache entry not found or expired", null));
            }

            return ResponseEntity.ok(new ApiResponse<>(true, "Cache entry retrieved", value));
        } catch (Exception e) {
            log.error("Error getting cached value", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to retrieve cache", null));
        }
    }

    /**
     * ENDPOINT 2: Get cache statistics
     * GET /api/v1/cache/stats/{cacheType}
     */
    @GetMapping("/stats/{cacheType}")
    public ResponseEntity<?> getCacheStatistics(
            @PathVariable String cacheType,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            CacheStatisticsResponse stats = cachingService.getCacheStatistics(cacheType, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Cache statistics retrieved", stats));
        } catch (CacheException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error getting cache statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to get statistics", null));
        }
    }

    /**
     * ENDPOINT 3: Get cached entries by type
     * GET /api/v1/cache/entries/{cacheType}?page=0&size=10
     */
    @GetMapping("/entries/{cacheType}")
    public ResponseEntity<?> getCachedEntries(
            @PathVariable String cacheType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            Page<CacheEntryResponse> entries = cachingService.getCachedEntries(cacheType, page, size, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Cache entries retrieved", entries));
        } catch (CacheException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error fetching cache entries", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch entries", null));
        }
    }
}