package com.eduplatform.admin.service;

import com.eduplatform.admin.model.AuditLog;
import com.eduplatform.admin.model.AuditAction;
import com.eduplatform.admin.model.AuditStatus;
import com.eduplatform.admin.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    // Log Action
    public void logAction(String adminId, AuditAction action, String targetId,
                          String targetType, String description) {
        try {
            AuditLog log = new AuditLog();
            log.setId(UUID.randomUUID().toString());
            log.setAdminId(adminId);
            log.setAction(action);
            log.setTargetId(targetId);
            log.setTargetType(targetType);
            log.setDescription(description);
            log.setStatus(AuditStatus.SUCCESS);
            log.setCreatedAt(LocalDateTime.now());

            auditLogRepository.save(log);

        } catch (Exception e) {
            log.error("Error logging audit action", e);
        }
    }

    // Log Action with Changes
    public void logActionWithChanges(String adminId, AuditAction action,
                                     String targetId, String targetType, String description,
                                     Map<String, Object> changesBefore, Map<String, Object> changesAfter) {
        try {
            AuditLog log = new AuditLog();
            log.setId(UUID.randomUUID().toString());
            log.setAdminId(adminId);
            log.setAction(action);
            log.setTargetId(targetId);
            log.setTargetType(targetType);
            log.setDescription(description);
            log.setChangesBefore(changesBefore);
            log.setChangesAfter(changesAfter);
            log.setStatus(AuditStatus.SUCCESS);
            log.setCreatedAt(LocalDateTime.now());

            auditLogRepository.save(log);

        } catch (Exception e) {
            log.error("Error logging audit action with changes", e);
        }
    }

    // Get Audit Logs
    public List<AuditLog> getAuditLogs(String adminId) {
        return auditLogRepository.findByAdminIdOrderByCreatedAtDesc(adminId);
    }

    // Get Logs for Entity
    public List<AuditLog> getLogsForEntity(String targetId, String targetType) {
        return auditLogRepository.findByTargetIdAndTargetTypeOrderByCreatedAtDesc(targetId, targetType);
    }
}
