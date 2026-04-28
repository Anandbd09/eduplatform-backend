package com.eduplatform.admin.controller;

import com.eduplatform.admin.service.PlatformStatisticsService;
import com.eduplatform.core.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/statistics")
public class StatisticsController {

    @Autowired
    private PlatformStatisticsService statisticsService;

    @GetMapping("/platform")
    public ResponseEntity<?> getPlatformStatistics() {
        try {
            var stats = statisticsService.getPlatformStatistics();
            return ResponseEntity.ok(ApiResponse.success(stats, "Platform statistics"));
        } catch (Exception e) {
            log.error("Error fetching platform statistics", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "PLATFORM_STATS_FAILED"));
        }
    }
}
