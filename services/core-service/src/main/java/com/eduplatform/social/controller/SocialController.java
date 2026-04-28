package com.eduplatform.social.controller;

import com.eduplatform.common.ApiResponse;
import com.eduplatform.social.service.SocialService;
import com.eduplatform.social.dto.*;
import com.eduplatform.social.exception.SocialException;
//import com.eduplatform.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/social")
public class SocialController {

    @Autowired
    private SocialService socialService;

    /**
     * ENDPOINT 1: Follow user
     * POST /api/v1/social/follow/{userId}
     */
    @PostMapping("/follow/{userId}")
    public ResponseEntity<?> followUser(
            @PathVariable String userId,
            @RequestHeader("X-User-Id") String currentUserId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            UserFollowResponse response = socialService.followUser(userId, currentUserId, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "User followed successfully", response));
        } catch (SocialException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error following user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to follow user", null));
        }
    }

    /**
     * ENDPOINT 2: Unfollow user
     * DELETE /api/v1/social/follow/{userId}
     */
    @DeleteMapping("/follow/{userId}")
    public ResponseEntity<?> unfollowUser(
            @PathVariable String userId,
            @RequestHeader("X-User-Id") String currentUserId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            socialService.unfollowUser(userId, currentUserId, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "User unfollowed successfully", null));
        } catch (Exception e) {
            log.error("Error unfollowing user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to unfollow user", null));
        }
    }

    /**
     * ENDPOINT 3: Get following list
     * GET /api/v1/social/following?page=0&size=10
     */
    @GetMapping("/following")
    public ResponseEntity<?> getFollowing(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            Page<UserFollowResponse> following = socialService.getFollowing(userId, page, size, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Following list retrieved", following));
        } catch (Exception e) {
            log.error("Error fetching following list", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch following list", null));
        }
    }

    /**
     * ENDPOINT 4: Get followers list
     * GET /api/v1/social/followers?page=0&size=10
     */
    @GetMapping("/followers")
    public ResponseEntity<?> getFollowers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            Page<UserFollowResponse> followers = socialService.getFollowers(userId, page, size, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Followers list retrieved", followers));
        } catch (Exception e) {
            log.error("Error fetching followers list", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch followers list", null));
        }
    }

    /**
     * ENDPOINT 5: Send message
     * POST /api/v1/social/messages/send
     */
    @PostMapping("/messages/send")
    public ResponseEntity<?> sendMessage(
            @RequestBody MessageRequest request,
            @RequestHeader("X-User-Id") String senderId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            MessageResponse response = socialService.sendMessage(request, senderId, tenantId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Message sent successfully", response));
        } catch (Exception e) {
            log.error("Error sending message", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to send message", null));
        }
    }

    /**
     * ENDPOINT 6: Get inbox
     * GET /api/v1/social/messages/inbox?page=0&size=10
     */
    @GetMapping("/messages/inbox")
    public ResponseEntity<?> getInbox(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            Page<MessageResponse> inbox = socialService.getInbox(userId, page, size, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Inbox retrieved", inbox));
        } catch (Exception e) {
            log.error("Error fetching inbox", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch inbox", null));
        }
    }
}