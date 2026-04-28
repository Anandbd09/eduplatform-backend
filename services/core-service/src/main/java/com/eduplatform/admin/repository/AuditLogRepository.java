package com.eduplatform.admin.repository;

import com.eduplatform.admin.model.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {
    List<AuditLog> findByAdminIdOrderByCreatedAtDesc(String adminId);

    List<AuditLog> findByTargetIdAndTargetTypeOrderByCreatedAtDesc(String targetId, String targetType);

    List<AuditLog> findByActionOrderByCreatedAtDesc(String action);

    List<AuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);

    List<AuditLog> findByStatusAndCreatedAtBetween(String status, LocalDateTime start, LocalDateTime end);
}