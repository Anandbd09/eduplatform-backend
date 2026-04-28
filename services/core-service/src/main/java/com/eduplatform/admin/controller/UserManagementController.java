package com.eduplatform.admin.controller;

import com.eduplatform.admin.dto.UserBanRequest;
import com.eduplatform.admin.service.UserManagementService;
import com.eduplatform.core.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/users")
public class UserManagementController {

    @Autowired
    private UserManagementService userManagementService;

    @PostMapping("/{userId}/ban")
    public ResponseEntity<?> banUser(@RequestHeader("X-User-Id") String adminId,
                                     @PathVariable String userId,
                                     @RequestBody UserBanRequest request) {
        try {
            userManagementService.banUser(
                    adminId,
                    userId,
                    request.getReason(),
                    request.getBanType(),
                    request.getBanDaysTemporary()
            );
            return ResponseEntity.ok(ApiResponse.success(null, "User banned"));
        } catch (Exception e) {
            log.error("Error banning user {}", userId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "USER_BAN_FAILED"));
        }
    }

    @PostMapping("/{userId}/unban")
    public ResponseEntity<?> unbanUser(@RequestHeader("X-User-Id") String adminId,
                                       @PathVariable String userId) {
        try {
            userManagementService.unbanUser(adminId, userId);
            return ResponseEntity.ok(ApiResponse.success(null, "User unbanned"));
        } catch (Exception e) {
            log.error("Error unbanning user {}", userId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "USER_UNBAN_FAILED"));
        }
    }

    @PostMapping("/{userId}/delete")
    public ResponseEntity<?> deleteUserAccount(@RequestHeader("X-User-Id") String adminId,
                                               @PathVariable String userId,
                                               @RequestParam String reason) {
        try {
            userManagementService.deleteUserAccount(adminId, userId, reason);
            return ResponseEntity.ok(ApiResponse.success(null, "User account deleted"));
        } catch (Exception e) {
            log.error("Error deleting user {}", userId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "USER_DELETE_FAILED"));
        }
    }

    @PostMapping("/{userId}/role")
    public ResponseEntity<?> changeUserRole(@RequestHeader("X-User-Id") String adminId,
                                            @PathVariable String userId,
                                            @RequestParam String newRole) {
        try {
            userManagementService.changeUserRole(adminId, userId, newRole);
            return ResponseEntity.ok(ApiResponse.success(null, "User role changed"));
        } catch (Exception e) {
            log.error("Error changing role for user {}", userId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "USER_ROLE_CHANGE_FAILED"));
        }
    }
}
