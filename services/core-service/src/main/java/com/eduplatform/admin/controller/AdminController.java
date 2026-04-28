package com.eduplatform.admin.controller;

import com.eduplatform.admin.service.AdminService;
import com.eduplatform.core.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/create")
    public ResponseEntity<?> createAdmin(@RequestHeader("X-User-Id") String adminId,
                                         @RequestParam String userId,
                                         @RequestParam String level) {
        try {
            var response = adminService.createAdmin(userId, level);
            return ResponseEntity.ok(ApiResponse.success(response, "Admin created"));
        } catch (Exception e) {
            log.error("Error creating admin for user {}", userId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "ADMIN_CREATE_FAILED"));
        }
    }

    @GetMapping("/{adminId}")
    public ResponseEntity<?> getAdmin(@PathVariable String adminId) {
        try {
            var response = adminService.getAdmin(adminId);
            return ResponseEntity.ok(ApiResponse.success(response, "Admin retrieved"));
        } catch (Exception e) {
            log.error("Error retrieving admin {}", adminId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "ADMIN_FETCH_FAILED"));
        }
    }
}
