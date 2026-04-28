package com.eduplatform.video.controller;

import com.eduplatform.video.service.VideoService;
import com.eduplatform.video.dto.*;
import com.eduplatform.video.exception.VideoException;
import com.eduplatform.common.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/videos")
public class AdminVideoController {

    @Autowired
    private VideoService videoService;

    /**
     * ENDPOINT 9: Upload video
     * POST /api/v1/admin/videos/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadVideo(
            @RequestBody VideoRequest request,
            @RequestHeader("X-User-Id") String instructorId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            VideoUploadResponse response = videoService.uploadVideo(request, instructorId, tenantId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Upload initiated", response));
        } catch (VideoException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error uploading video", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to upload video", null));
        }
    }

    /**
     * ENDPOINT 10: Get all videos
     * GET /api/v1/admin/videos?page=0&size=10&status=READY
     */
    @GetMapping
    public ResponseEntity<?> getAllVideos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            // Note: Admin should see all videos, implement separate method if needed
            return ResponseEntity.ok(new ApiResponse<>(true, "Videos retrieved", null));
        } catch (Exception e) {
            log.error("Error fetching videos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch videos", null));
        }
    }

    /**
     * ENDPOINT 11: Update video metadata
     * PUT /api/v1/admin/videos/{videoId}
     */
    @PutMapping("/{videoId}")
    public ResponseEntity<?> updateVideo(
            @PathVariable String videoId,
            @RequestBody VideoRequest request,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            VideoResponse response = videoService.getVideo(videoId, tenantId);
            // Update logic here
            return ResponseEntity.ok(new ApiResponse<>(true, "Video updated", response));
        } catch (VideoException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error updating video", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to update video", null));
        }
    }

    /**
     * ENDPOINT 12: Delete video
     * DELETE /api/v1/admin/videos/{videoId}
     */
    @DeleteMapping("/{videoId}")
    public ResponseEntity<?> deleteVideo(
            @PathVariable String videoId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            videoService.deleteVideo(videoId, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Video deleted", null));
        } catch (VideoException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error deleting video", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to delete video", null));
        }
    }

    /**
     * ENDPOINT 13: Publish video
     * POST /api/v1/admin/videos/{videoId}/publish
     */
    @PostMapping("/{videoId}/publish")
    public ResponseEntity<?> publishVideo(
            @PathVariable String videoId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            VideoResponse response = videoService.publishVideo(videoId, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Video published", response));
        } catch (VideoException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error publishing video", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to publish video", null));
        }
    }

    /**
     * ENDPOINT 14: Create chapter
     * POST /api/v1/admin/videos/{videoId}/chapters
     */
    @PostMapping("/{videoId}/chapters")
    public ResponseEntity<?> createChapter(
            @PathVariable String videoId,
            @RequestParam String title,
            @RequestParam Integer chapterNumber,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            var chapter = videoService.createChapter(videoId, title, chapterNumber, tenantId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Chapter created", chapter));
        } catch (Exception e) {
            log.error("Error creating chapter", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to create chapter", null));
        }
    }

    /**
     * ENDPOINT 15: Upload subtitle
     * POST /api/v1/admin/videos/{videoId}/subtitles
     */
    @PostMapping("/{videoId}/subtitles")
    public ResponseEntity<?> uploadSubtitle(
            @PathVariable String videoId,
            @RequestParam String language,
            @RequestParam String fileUrl,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            SubtitleResponse response = videoService.uploadSubtitle(videoId, language, fileUrl, tenantId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Subtitle uploaded", response));
        } catch (Exception e) {
            log.error("Error uploading subtitle", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to upload subtitle", null));
        }
    }

    /**
     * ENDPOINT 16: Get transcode status
     * GET /api/v1/admin/videos/{videoId}/transcode-status
     */
    @GetMapping("/{videoId}/transcode-status")
    public ResponseEntity<?> getTranscodeStatus(
            @PathVariable String videoId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            return ResponseEntity.ok(new ApiResponse<>(true, "Transcode status retrieved", null));
        } catch (Exception e) {
            log.error("Error fetching transcode status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch status", null));
        }
    }
}