package com.eduplatform.security.controller;

import com.eduplatform.common.ApiResponse;
import com.eduplatform.security.service.AdvancedSecurityService;
import com.eduplatform.security.dto.*;
import com.eduplatform.security.exception.SecurityException;
//import com.eduplatform.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/security")
public class AdminSecurityController {

    @Autowired
    private AdvancedSecurityService securityService;

    /**
     * ENDPOINT 5: Disable 2FA (admin override)
     * POST /api/v1/admin/security/2fa/disable/{userId}
     */
    @PostMapping("/2fa/disable/{userId}")
    public ResponseEntity<?> disableTwoFactorAuth(
            @PathVariable String userId,
            @RequestParam String password,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            securityService.disableTwoFactorAuth(userId, password, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "2FA disabled", null));
        } catch (SecurityException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error disabling 2FA", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to disable 2FA", null));
        }
    }

    /**
     * ENDPOINT 6: Add IP to whitelist
     * POST /api/v1/admin/security/whitelist/add
     */
    @PostMapping("/whitelist/add")
    public ResponseEntity<?> addIpToWhitelist(
            @RequestBody IpWhitelistRequest request,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            IpWhitelistResponse response = securityService.addIpToWhitelist(
                    request.getUserId(), request.getIpAddress(), request.getDescription(), tenantId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "IP whitelisted", response));
        } catch (Exception e) {
            log.error("Error adding IP to whitelist", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to whitelist IP", null));
        }
    }

    /**
     * ENDPOINT 7: Remove IP from whitelist
     * DELETE /api/v1/admin/security/whitelist/{whitelistId}
     */
    @DeleteMapping("/whitelist/{whitelistId}")
    public ResponseEntity<?> removeIpFromWhitelist(
            @PathVariable String whitelistId,
            @RequestParam String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            securityService.removeIpFromWhitelist(userId, whitelistId, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "IP removed from whitelist", null));
        } catch (SecurityException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error removing IP from whitelist", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to remove IP", null));
        }
    }

    /**
     * ENDPOINT 8: Revoke device
     * POST /api/v1/admin/security/device/revoke/{userId}/{deviceId}
     */
    @PostMapping("/device/revoke/{userId}/{deviceId}")
    public ResponseEntity<?> revokeDevice(
            @PathVariable String userId,
            @PathVariable String deviceId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            securityService.revokeDevice(userId, deviceId, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Device revoked", null));
        } catch (SecurityException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error revoking device", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to revoke device", null));
        }
    }
}