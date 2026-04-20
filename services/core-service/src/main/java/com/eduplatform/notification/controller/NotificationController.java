package com.eduplatform.notification.controller;

import com.eduplatform.core.common.response.ApiResponse;
import com.eduplatform.notification.dto.NotificationRequest;
import com.eduplatform.notification.model.Notification;
import com.eduplatform.notification.service.InAppNotificationService;
import com.eduplatform.notification.service.NotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final InAppNotificationService inAppService;
    private final NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Void>> sendNotification(
            @RequestBody NotificationRequest request) {
        try {
            notificationService.sendNotification(request);
            return ResponseEntity.ok(ApiResponse.success(null, "Notification sent"));
        } catch (Exception e) {
            log.error("Failed to send notification", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "NOTIFICATION_SEND_FAILED", "Unable to send notification"));
        }
    }

    // Get Unread Notifications
    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<Notification>>> getUnreadNotifications(
            @RequestHeader("X-User-Id") String userId) {
        try {
            List<Notification> notifications = inAppService.getUnreadNotifications(userId);
            return ResponseEntity.ok(ApiResponse.success(notifications, "Unread notifications"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "NOTIFICATION_FETCH_FAILED", "Unable to fetch unread notifications"));
        }
    }

    // Get All Notifications
    @GetMapping
    public ResponseEntity<ApiResponse<List<Notification>>> getAllNotifications(
            @RequestHeader("X-User-Id") String userId) {
        try {
            List<Notification> notifications = inAppService.getAllNotifications(userId);
            return ResponseEntity.ok(ApiResponse.success(notifications, "All notifications"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "NOTIFICATION_FETCH_FAILED", "Unable to fetch notifications"));
        }
    }

    // Mark as Read
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable String notificationId) {
        try {
            inAppService.markAsRead(notificationId);
            return ResponseEntity.ok(ApiResponse.success(null, "Marked as read"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "NOTIFICATION_UPDATE_FAILED", "Unable to mark notification as read"));
        }
    }

    // Mark All as Read
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@RequestHeader("X-User-Id") String userId) {
        try {
            inAppService.markAllAsRead(userId);
            return ResponseEntity.ok(ApiResponse.success(null, "All marked as read"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "NOTIFICATION_UPDATE_FAILED", "Unable to mark all notifications as read"));
        }
    }

    // Delete Notification
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable String notificationId) {
        try {
            inAppService.deleteNotification(notificationId);
            return ResponseEntity.ok(ApiResponse.success(null, "Notification deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "NOTIFICATION_DELETE_FAILED", "Unable to delete notification"));
        }
    }

    // Get Unread Count
    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(@RequestHeader("X-User-Id") String userId) {
        try {
            Long count = inAppService.getUnreadCount(userId);
            return ResponseEntity.ok(ApiResponse.success(count, "Unread count"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "NOTIFICATION_FETCH_FAILED", "Unable to fetch unread count"));
        }
    }
}
