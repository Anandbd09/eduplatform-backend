package com.eduplatform.admin.service;

import com.eduplatform.admin.model.AuditAction;
import com.eduplatform.admin.model.UserReport;
import com.eduplatform.admin.repository.UserReportRepository;
import com.eduplatform.core.user.model.User;
import com.eduplatform.core.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
public class UserManagementService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserReportRepository userReportRepository;

    @Autowired
    private AuditLogService auditLogService;

    public List<User> getAllUsers(int page, int size) {
        return userRepository.findAll().stream()
                .skip((long) page * size)
                .limit(size)
                .toList();
    }

    @Transactional
    public void banUser(String adminId, String userId, String reason, String banType, Integer banDaysTemporary) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            LocalDateTime now = LocalDateTime.now();
            String normalizedBanType = normalizeValue(banType);

            if ("TEMPORARY".equals(normalizedBanType)) {
                if (banDaysTemporary == null || banDaysTemporary <= 0) {
                    throw new RuntimeException("Temporary bans require a positive ban duration");
                }
                user.setBannedUntil(now.plusDays(banDaysTemporary));
            } else {
                user.setBannedUntil(null);
            }

            user.setStatus("BANNED");
            user.setBanReason(reason);
            user.setBannedAt(now);
            user.setSuspendedUntil(null);
            user.setSuspensionReason(null);
            user.setUpdatedAt(now);

            userRepository.save(user);

            auditLogService.logAction(
                    adminId,
                    AuditAction.USER_BANNED,
                    userId,
                    "USER",
                    "User banned: " + reason + " (" + normalizedBanType + ")"
            );

            log.info("User banned: {} by admin: {}", userId, adminId);
        } catch (Exception e) {
            log.error("Error banning user", e);
            throw new RuntimeException("Failed to ban user");
        }
    }

    @Transactional
    public void unbanUser(String adminId, String userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setStatus("ACTIVE");
            user.setBannedUntil(null);
            user.setBanReason(null);
            user.setBannedAt(null);
            user.setUpdatedAt(LocalDateTime.now());

            userRepository.save(user);

            auditLogService.logAction(
                    adminId,
                    AuditAction.USER_UNBANNED,
                    userId,
                    "USER",
                    "User unbanned"
            );
        } catch (Exception e) {
            log.error("Error unbanning user", e);
            throw new RuntimeException("Failed to unban user");
        }
    }

    @Transactional
    public void suspendUser(String adminId, String userId, String reason, Integer suspensionDays) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            LocalDateTime now = LocalDateTime.now();
            if (suspensionDays == null || suspensionDays <= 0) {
                throw new RuntimeException("Suspension requires a positive duration");
            }

            user.setStatus("SUSPENDED");
            user.setSuspendedUntil(now.plusDays(suspensionDays));
            user.setSuspensionReason(reason);
            user.setUpdatedAt(now);

            userRepository.save(user);

            auditLogService.logAction(
                    adminId,
                    AuditAction.USER_UPDATED,
                    userId,
                    "USER",
                    "User suspended for " + suspensionDays + " days: " + reason
            );
        } catch (Exception e) {
            log.error("Error suspending user", e);
            throw new RuntimeException("Failed to suspend user");
        }
    }

    @Transactional
    public void checkAndUnbanExpiredBans() {
        List<User> usersToUnban = userRepository.findAll().stream()
                .filter(user -> "BANNED".equalsIgnoreCase(user.getStatus()))
                .filter(user -> user.getBannedUntil() != null && user.getBannedUntil().isBefore(LocalDateTime.now()))
                .toList();

        for (User user : usersToUnban) {
            user.setStatus("ACTIVE");
            user.setBannedUntil(null);
            user.setBanReason(null);
            user.setBannedAt(null);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);

            log.info("User auto-unbanned: {}", user.getId());
        }
    }

    @Transactional
    public void deleteUserAccount(String adminId, String userId, String reason) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setStatus("DELETED");
            user.setEmail(null);
            user.setPhone(null);
            user.setPasswordHash(null);
            user.setBannedUntil(null);
            user.setBanReason(null);
            user.setBannedAt(null);
            user.setSuspendedUntil(null);
            user.setSuspensionReason(null);
            user.setDeletedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());

            userRepository.save(user);

            auditLogService.logAction(
                    adminId,
                    AuditAction.USER_DELETED,
                    userId,
                    "USER",
                    "User account deleted: " + reason
            );
        } catch (Exception e) {
            log.error("Error deleting user account", e);
            throw new RuntimeException("Failed to delete user account");
        }
    }

    @Transactional
    public void changeUserRole(String adminId, String userId, String newRole) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String oldRole = user.getRole();
            user.setRole(normalizeValue(newRole));
            user.setUpdatedAt(LocalDateTime.now());

            userRepository.save(user);

            auditLogService.logAction(
                    adminId,
                    AuditAction.USER_ROLE_CHANGED,
                    userId,
                    "USER",
                    "Role changed from " + oldRole + " to " + user.getRole()
            );
        } catch (Exception e) {
            log.error("Error changing user role", e);
            throw new RuntimeException("Failed to change user role");
        }
    }

    public List<UserReport> getUserReports(String userId) {
        return userReportRepository.findByReportedUserId(userId);
    }

    private String normalizeValue(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }
}
