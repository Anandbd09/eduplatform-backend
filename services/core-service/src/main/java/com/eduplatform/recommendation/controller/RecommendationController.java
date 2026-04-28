package com.eduplatform.recommendation.controller;

import com.eduplatform.recommendation.service.RecommendationService;
import com.eduplatform.recommendation.dto.*;
import com.eduplatform.recommendation.exception.RecommendationException;
import com.eduplatform.core.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/recommendations")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    /**
     * ENDPOINT 1: Get personalized recommendations
     * GET /api/v1/recommendations/personalized?page=0&size=10
     */
    @GetMapping("/personalized")
    public ResponseEntity<?> getPersonalizedRecommendations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            Page<RecommendationResponse> recommendations = recommendationService
                    .getPersonalizedRecommendations(userId, page, size, tenantId);
            return ResponseEntity.ok(ApiResponse.success(recommendations, "Recommendations retrieved"));
        } catch (Exception e) {
            log.error("Error fetching personalized recommendations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch recommendations", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * ENDPOINT 2: Track interaction
     * POST /api/v1/recommendations/track?courseId=X&interactionType=VIEW&rating=5
     */
    @PostMapping("/track")
    public ResponseEntity<?> trackInteraction(
            @RequestParam String courseId,
            @RequestParam String interactionType,
            @RequestParam(required = false) Double rating,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            recommendationService.trackInteraction(userId, courseId, interactionType, rating, tenantId);
            return ResponseEntity.ok(ApiResponse.success(null, "Interaction tracked"));
        } catch (RecommendationException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "RECOMMENDATION_ERROR"));
        } catch (Exception e) {
            log.error("Error tracking interaction", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to track interaction", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * ENDPOINT 3: Mark recommendation as clicked
     * POST /api/v1/recommendations/{recommendationId}/click
     */
    @PostMapping("/{recommendationId}/click")
    public ResponseEntity<?> markClicked(
            @PathVariable String recommendationId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            recommendationService.markRecommendationClicked(recommendationId, tenantId);
            return ResponseEntity.ok(ApiResponse.success(null, "Recommendation marked as clicked"));
        } catch (Exception e) {
            log.error("Error marking recommendation as clicked", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to mark as clicked", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * ENDPOINT 4: Get trending courses
     * GET /api/v1/recommendations/trending?limit=10
     */
    @GetMapping("/trending")
    public ResponseEntity<?> getTrendingCourses(
            @RequestParam(defaultValue = "10") int limit,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            var trending = recommendationService.getTrendingCourses(limit, tenantId);
            return ResponseEntity.ok(ApiResponse.success(trending, "Trending courses retrieved"));
        } catch (Exception e) {
            log.error("Error fetching trending courses", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch trending courses", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * ENDPOINT 5: Get similar courses
     * GET /api/v1/recommendations/similar/{courseId}?limit=5
     */
    @GetMapping("/similar/{courseId}")
    public ResponseEntity<?> getSimilarCourses(
            @PathVariable String courseId,
            @RequestParam(defaultValue = "5") int limit,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            var similar = recommendationService.getSimilarCourses(courseId, limit, tenantId);
            return ResponseEntity.ok(ApiResponse.success(similar, "Similar courses retrieved"));
        } catch (Exception e) {
            log.error("Error fetching similar courses", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch similar courses", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * ENDPOINT 6: Get recommendations by reason
     * GET /api/v1/recommendations/by-reason/{reason}?page=0&size=10
     */
    @GetMapping("/by-reason/{reason}")
    public ResponseEntity<?> getByReason(
            @PathVariable String reason,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            Page<RecommendationResponse> recommendations = recommendationService
                    .getRecommendationsByReason(userId, reason, page, size, tenantId);
            return ResponseEntity.ok(ApiResponse.success(recommendations, "Recommendations retrieved"));
        } catch (Exception e) {
            log.error("Error fetching recommendations by reason", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch recommendations", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * ENDPOINT 7: Get user analytics
     * GET /api/v1/recommendations/analytics/user
     */
    @GetMapping("/analytics/user")
    public ResponseEntity<?> getUserAnalytics(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            RecommendationAnalytics analytics = recommendationService.getUserAnalytics(userId, tenantId);
            return ResponseEntity.ok(ApiResponse.success(analytics, "Analytics retrieved"));
        } catch (Exception e) {
            log.error("Error fetching user analytics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch analytics", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * ENDPOINT 8: Get clicked recommendations
     * GET /api/v1/recommendations/clicked?page=0&size=10
     */
    @GetMapping("/clicked")
    public ResponseEntity<?> getClickedRecommendations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            Page<RecommendationResponse> recommendations = recommendationService
                    .getClickedRecommendations(userId, page, size, tenantId);
            return ResponseEntity.ok(ApiResponse.success(recommendations, "Clicked recommendations retrieved"));
        } catch (Exception e) {
            log.error("Error fetching clicked recommendations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch recommendations", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * ENDPOINT 9: Recalculate for user
     * POST /api/v1/recommendations/recalculate
     */
    @PostMapping("/recalculate")
    public ResponseEntity<?> recalculateRecommendations(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            recommendationService.recalculateForUser(userId, tenantId);
            return ResponseEntity.ok(ApiResponse.success(null, "Recommendations recalculated"));
        } catch (Exception e) {
            log.error("Error recalculating recommendations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to recalculate recommendations", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * ENDPOINT 10: Health check (bonus)
     * GET /api/v1/recommendations/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(ApiResponse.success(null, "Recommendation service is healthy"));
    }
}
