package com.eduplatform.gamification.controller;

import com.eduplatform.common.ApiResponse;
import com.eduplatform.gamification.service.GamificationService;
import com.eduplatform.gamification.dto.*;
import com.eduplatform.gamification.exception.GamificationException;
//import com.eduplatform.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/gamification")
public class GamificationController {

    @Autowired
    private GamificationService gamificationService;

    /**
     * ENDPOINT 1: Get user streak
     * GET /api/v1/gamification/streak/{courseId}
     */
    @GetMapping("/streak/{courseId}")
    public ResponseEntity<?> getUserStreak(
            @PathVariable String courseId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            StreakResponse streak = gamificationService.getUserStreak(courseId, userId, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Streak retrieved", streak));
        } catch (Exception e) {
            log.error("Error getting user streak", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to get streak", null));
        }
    }

    /**
     * ENDPOINT 2: Get user points
     * GET /api/v1/gamification/points
     */
    @GetMapping("/points")
    public ResponseEntity<?> getUserPoints(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            PointsResponse points = gamificationService.getUserPoints(userId, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Points retrieved", points));
        } catch (Exception e) {
            log.error("Error getting user points", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to get points", null));
        }
    }

    /**
     * ENDPOINT 3: Get achievements
     * GET /api/v1/gamification/achievements?page=0&size=10
     */
    @GetMapping("/achievements")
    public ResponseEntity<?> getUserAchievements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            Page<AchievementResponse> achievements = gamificationService.getUserAchievements(userId, page, size, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Achievements retrieved", achievements));
        } catch (Exception e) {
            log.error("Error getting achievements", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to get achievements", null));
        }
    }

    /**
     * ENDPOINT 4: Get leaderboard
     * GET /api/v1/gamification/leaderboard?page=0&size=10
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<?> getLeaderboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            Page<LeaderboardEntryResponse> leaderboard = gamificationService.getLeaderboard(page, size, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Leaderboard retrieved", leaderboard));
        } catch (Exception e) {
            log.error("Error getting leaderboard", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to get leaderboard", null));
        }
    }

    /**
     * ENDPOINT 5: Record lesson completion
     * POST /api/v1/gamification/lesson-complete/{courseId}
     */
    @PostMapping("/lesson-complete/{courseId}")
    public ResponseEntity<?> recordLessonCompletion(
            @PathVariable String courseId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            StreakResponse streak = gamificationService.recordLessonCompletion(courseId, userId, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Lesson completion recorded", streak));
        } catch (Exception e) {
            log.error("Error recording lesson completion", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to record lesson completion", null));
        }
    }
}