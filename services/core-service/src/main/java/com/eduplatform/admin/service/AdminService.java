package com.eduplatform.admin.service;

import com.eduplatform.admin.dto.AdminResponse;
import com.eduplatform.admin.model.*;
import com.eduplatform.admin.repository.AdminRepository;
import com.eduplatform.core.user.model.User;
import com.eduplatform.core.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Transactional
    public AdminResponse createAdmin(String userId, String level) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            AdminLevel adminLevel = AdminLevel.valueOf(normalizeValue(level));
            LocalDateTime now = LocalDateTime.now();

            Admin admin = new Admin();
            admin.setId(UUID.randomUUID().toString());
            admin.setUserId(userId);
            admin.setLevel(adminLevel);
            admin.setPermissions(getDefaultPermissions(adminLevel));
            admin.setStatus(AdminStatus.ACTIVE);
            admin.setCreatedAt(now);
            admin.setUpdatedAt(now);
            admin.setTotalActionsPerformed(0);
            admin.setTenantId(user.getTenantId());

            Admin savedAdmin = adminRepository.save(admin);

            auditLogService.logAction(
                    userId,
                    AuditAction.ADMIN_CREATED,
                    savedAdmin.getId(),
                    "ADMIN",
                    "Admin " + adminLevel + " created for user: " + user.getEmail()
            );

            return convertToResponse(savedAdmin);
        } catch (Exception e) {
            log.error("Error creating admin", e);
            throw new RuntimeException("Failed to create admin");
        }
    }

    public AdminResponse getAdmin(String adminId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        return convertToResponse(admin);
    }

    @Transactional
    public void updateAdminPermissions(String adminId, List<String> permissions) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        List<AdminPermission> newPermissions = permissions.stream()
                .map(this::normalizeValue)
                .map(AdminPermission::valueOf)
                .toList();

        admin.setPermissions(newPermissions);
        admin.setUpdatedAt(LocalDateTime.now());
        adminRepository.save(admin);

        auditLogService.logAction(
                adminId,
                AuditAction.ADMIN_PERMISSIONS_CHANGED,
                adminId,
                "ADMIN",
                "Permissions updated"
        );
    }

    @Transactional
    public void deleteAdmin(String adminId) {
        adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        adminRepository.deleteById(adminId);

        auditLogService.logAction(
                adminId,
                AuditAction.ADMIN_DELETED,
                adminId,
                "ADMIN",
                "Admin deleted"
        );
    }

    public boolean hasPermission(String adminId, String permission) {
        Admin admin = adminRepository.findById(adminId).orElse(null);

        if (admin == null || permission == null || permission.isBlank()) {
            return false;
        }

        if (admin.getLevel() == AdminLevel.SUPER_ADMIN) {
            return true;
        }

        return admin.getPermissions() != null
                && admin.getPermissions().contains(AdminPermission.valueOf(normalizeValue(permission)));
    }

    private List<AdminPermission> getDefaultPermissions(AdminLevel level) {
        return switch (level) {
            case SUPER_ADMIN -> Arrays.asList(AdminPermission.values());
            case ADMIN -> List.of(
                    AdminPermission.MANAGE_USERS,
                    AdminPermission.MANAGE_COURSES,
                    AdminPermission.MANAGE_PAYMENTS,
                    AdminPermission.VIEW_ANALYTICS,
                    AdminPermission.MANAGE_DISPUTES,
                    AdminPermission.BAN_USERS,
                    AdminPermission.DELETE_COURSES,
                    AdminPermission.VIEW_AUDIT_LOGS
            );
            case MODERATOR -> List.of(
                    AdminPermission.MANAGE_COURSES,
                    AdminPermission.VIEW_AUDIT_LOGS
            );
        };
    }

    private AdminResponse convertToResponse(Admin admin) {
        AdminResponse response = new AdminResponse();
        response.setAdminId(admin.getId());
        response.setUserId(admin.getUserId());
        response.setAdminLevel(admin.getLevel().toString());
        response.setPermissions(admin.getPermissions().stream()
                .map(Enum::toString)
                .toList());
        response.setStatus(admin.getStatus().toString());
        response.setLastLoginAt(admin.getLastLoginAt());
        response.setCreatedAt(admin.getCreatedAt());
        response.setTotalActionsPerformed(admin.getTotalActionsPerformed());

        return response;
    }

    private String normalizeValue(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }
}
