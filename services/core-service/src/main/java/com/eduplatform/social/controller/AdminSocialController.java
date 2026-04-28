package com.eduplatform.social.controller;

import com.eduplatform.common.ApiResponse;
import com.eduplatform.social.service.SocialService;
import com.eduplatform.social.service.ForumService;
import com.eduplatform.social.dto.*;
import com.eduplatform.social.exception.SocialException;
//import com.eduplatform.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/social")
public class AdminSocialController {

    @Autowired
    private SocialService socialService;

    @Autowired
    private ForumService forumService;

    /**
     * ENDPOINT 7: Create forum thread
     * POST /api/v1/admin/social/forum/threads
     */
    @PostMapping("/forum/threads")
    public ResponseEntity<?> createForumThread(
            @RequestBody ForumThreadRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            ForumThreadResponse response = socialService.createForumThread(request, userId, tenantId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Forum thread created", response));
        } catch (Exception e) {
            log.error("Error creating forum thread", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to create forum thread", null));
        }
    }

    /**
     * ENDPOINT 8: Post forum reply
     * POST /api/v1/admin/social/forum/posts
     */
    @PostMapping("/forum/posts")
    public ResponseEntity<?> postForumReply(
            @RequestBody ForumPostRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            ForumPostResponse response = socialService.postReply(request, userId, tenantId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Forum post created", response));
        } catch (Exception e) {
            log.error("Error posting forum reply", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to post forum reply", null));
        }
    }

    /**
     * ENDPOINT 9: Pin thread
     * POST /api/v1/admin/social/forum/threads/{threadId}/pin
     */
    @PostMapping("/forum/threads/{threadId}/pin")
    public ResponseEntity<?> pinThread(
            @PathVariable String threadId) {
        try {
            forumService.pinThread(threadId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Thread pinned successfully", null));
        } catch (Exception e) {
            log.error("Error pinning thread", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to pin thread", null));
        }
    }

    /**
     * ENDPOINT 10: Lock thread
     * POST /api/v1/admin/social/forum/threads/{threadId}/lock
     */
    @PostMapping("/forum/threads/{threadId}/lock")
    public ResponseEntity<?> lockThread(
            @PathVariable String threadId) {
        try {
            forumService.lockThread(threadId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Thread locked successfully", null));
        } catch (Exception e) {
            log.error("Error locking thread", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to lock thread", null));
        }
    }

    /**
     * ENDPOINT 11: Like content
     * POST /api/v1/admin/social/content/like
     */
    @PostMapping("/content/like")
    public ResponseEntity<?> likeContent(
            @RequestBody LikeRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            socialService.likeContent(request, userId, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Content liked successfully", null));
        } catch (SocialException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error liking content", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to like content", null));
        }
    }

    /**
     * ENDPOINT 12: Health check
     * GET /api/v1/admin/social/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        try {
            return ResponseEntity.ok(new ApiResponse<>(true, "Social service is healthy", null));
        } catch (Exception e) {
            log.error("Error checking social health", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Social service health check failed", null));
        }
    }
}