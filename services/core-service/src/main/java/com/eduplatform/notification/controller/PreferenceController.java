package com.eduplatform.notification.controller;

import com.eduplatform.core.common.response.ApiResponse;
import com.eduplatform.notification.dto.PreferenceResponse;
import com.eduplatform.notification.service.PreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notification-preferences")
public class PreferenceController {

    private final PreferenceService preferenceService;

    // Get Preferences
    @GetMapping
    public ResponseEntity<ApiResponse<PreferenceResponse>> getPreferences(
            @RequestHeader("X-User-Id") String userId) {
        try {
            PreferenceResponse preference = preferenceService.getPreferences(userId);
            return ResponseEntity.ok(ApiResponse.success(preference, "Preferences retrieved"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "PREFERENCE_FETCH_FAILED", "Unable to fetch preferences"));
        }
    }

    // Update Preferences
    @PutMapping
    public ResponseEntity<ApiResponse<PreferenceResponse>> updatePreferences(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody PreferenceResponse request) {
        try {
            PreferenceResponse updatedPreference = preferenceService.updatePreferences(userId, request);
            return ResponseEntity.ok(ApiResponse.success(updatedPreference, "Preferences updated"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "PREFERENCE_UPDATE_FAILED", "Unable to update preferences"));
        }
    }

    // Toggle Email Notifications
    @PutMapping("/email/toggle")
    public ResponseEntity<ApiResponse<PreferenceResponse>> toggleEmailNotifications(
            @RequestHeader("X-User-Id") String userId) {
        try {
            PreferenceResponse updatedPreference = preferenceService.toggleEmailNotifications(userId);
            return ResponseEntity.ok(ApiResponse.success(updatedPreference, "Email notifications toggled"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "PREFERENCE_UPDATE_FAILED", "Unable to toggle email notifications"));
        }
    }

    // Unsubscribe
    @PostMapping("/unsubscribe")
    public ResponseEntity<ApiResponse<PreferenceResponse>> unsubscribe(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam String channel) {
        try {
            PreferenceResponse updatedPreference = preferenceService.unsubscribeFromChannel(userId, channel);
            return ResponseEntity.ok(ApiResponse.success(updatedPreference, "Unsubscribed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "PREFERENCE_UPDATE_FAILED", "Unable to unsubscribe from channel"));
        }
    }
}
