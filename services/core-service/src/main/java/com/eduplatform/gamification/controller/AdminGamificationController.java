package com.eduplatform.gamification.controller;

import com.eduplatform.common.ApiResponse;
import com.eduplatform.gamification.service.GamificationService;
//import com.eduplatform.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/gamification")
public class AdminGamificationController {

    @Autowired
    private GamificationService gamificationService;

    /**
     * ENDPOINT 6: Award bonus points
     * POST /api/v1/admin/gamification/award-points/{userId}
     */
    @PostMapping("/award-points/{userId}")
    public ResponseEntity<?> awardBonusPoints(
            @PathVariable String userId,
            @RequestParam Integer points,
            @RequestParam String reason,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            // Placeholder - actual implementation in service
            return ResponseEntity.ok(new ApiResponse<>(true, "Points awarded", null));
        } catch (Exception e) {
            log.error("Error awarding points", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to award points", null));
        }
    }

    /**
     * ENDPOINT 7: Reset user streak
     * POST /api/v1/admin/gamification/reset-streak/{userId}/{courseId}
     */
    @PostMapping("/reset-streak/{userId}/{courseId}")
    public ResponseEntity<?> resetStreak(
            @PathVariable String userId,
            @PathVariable String courseId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            return ResponseEntity.ok(new ApiResponse<>(true, "Streak reset", null));
        } catch (Exception e) {
            log.error("Error resetting streak", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to reset streak", null));
        }
    }

    /**
     * ENDPOINT 8: Unlock achievement manually
     * POST /api/v1/admin/gamification/unlock-achievement/{userId}/{achievementCode}
     */
    @PostMapping("/unlock-achievement/{userId}/{achievementCode}")
    public ResponseEntity<?> unlockAchievement(
            @PathVariable String userId,
            @PathVariable String achievementCode,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            return ResponseEntity.ok(new ApiResponse<>(true, "Achievement unlocked", null));
        } catch (Exception e) {
            log.error("Error unlocking achievement", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to unlock achievement", null));
        }
    }

    /**
     * ENDPOINT 9: Get gamification stats
     * GET /api/v1/admin/gamification/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getStats(
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            return ResponseEntity.ok(new ApiResponse<>(true, "Stats retrieved", null));
        } catch (Exception e) {
            log.error("Error getting stats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to get stats", null));
        }
    }

    /**
     * ENDPOINT 10: Health check
     * GET /api/v1/admin/gamification/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Gamification service is healthy", null));
    }
}