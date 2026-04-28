package com.eduplatform.video.controller;

import com.eduplatform.video.service.VideoService;
import com.eduplatform.video.dto.*;
import com.eduplatform.video.exception.VideoException;
import com.eduplatform.common.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/videos")
public class VideoController {

    @Autowired
    private VideoService videoService;

    /**
     * ENDPOINT 1: Get videos in course
     * GET /api/v1/videos?courseId=&page=0&size=10
     */
    @GetMapping
    public ResponseEntity<?> getVideos(
            @RequestParam String courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            Page<VideoResponse> videos = videoService.getCourseVideos(courseId, page, size, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Videos retrieved", videos));
        } catch (Exception e) {
            log.error("Error fetching videos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch videos", null));
        }
    }

    /**
     * ENDPOINT 2: Get video metadata
     * GET /api/v1/videos/{videoId}
     */
    @GetMapping("/{videoId}")
    public ResponseEntity<?> getVideo(
            @PathVariable String videoId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            VideoResponse video = videoService.getVideo(videoId, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Video retrieved", video));
        } catch (VideoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error fetching video", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch video", null));
        }
    }

    /**
     * ENDPOINT 3: Get streaming URL (HLS/DASH)
     * GET /api/v1/videos/{videoId}/stream
     */
    @GetMapping("/{videoId}/stream")
    public ResponseEntity<?> getStreamingUrl(
            @PathVariable String videoId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            StreamingManifestResponse response = videoService.getStreamingUrl(videoId, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Streaming URLs retrieved", response));
        } catch (VideoException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error getting streaming URL", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to get streaming URL", null));
        }
    }

    /**
     * ENDPOINT 4: Update playback progress
     * POST /api/v1/videos/{videoId}/playback
     */
    @PostMapping("/{videoId}/playback")
    public ResponseEntity<?> updatePlayback(
            @PathVariable String videoId,
            @RequestBody PlaybackRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            PlaybackResponse response = videoService.updatePlayback(videoId, request, userId, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Playback updated", response));
        } catch (Exception e) {
            log.error("Error updating playback", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to update playback", null));
        }
    }

    /**
     * ENDPOINT 5: Get user playback history
     * GET /api/v1/videos/{videoId}/playback?page=0&size=10
     */
    @GetMapping("/{videoId}/playback")
    public ResponseEntity<?> getPlaybackHistory(
            @PathVariable String videoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            Page<PlaybackResponse> playbacks = videoService.getUserPlaybacks(userId, page, size, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Playback history retrieved", playbacks));
        } catch (Exception e) {
            log.error("Error fetching playback history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch history", null));
        }
    }

    /**
     * ENDPOINT 6: Get subtitles for video
     * GET /api/v1/videos/{videoId}/subtitles
     */
    @GetMapping("/{videoId}/subtitles")
    public ResponseEntity<?> getSubtitles(
            @PathVariable String videoId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            var subtitles = videoService.getSubtitles(videoId, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Subtitles retrieved", subtitles));
        } catch (Exception e) {
            log.error("Error fetching subtitles", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch subtitles", null));
        }
    }

    /**
     * ENDPOINT 7: Get video analytics
     * GET /api/v1/videos/{videoId}/analytics
     */
    @GetMapping("/{videoId}/analytics")
    public ResponseEntity<?> getAnalytics(
            @PathVariable String videoId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            var analytics = videoService.getAnalytics(videoId, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Analytics retrieved", analytics));
        } catch (Exception e) {
            log.error("Error fetching analytics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch analytics", null));
        }
    }

    /**
     * ENDPOINT 8: Health check
     * GET /api/v1/videos/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Video service is healthy", null));
    }
}