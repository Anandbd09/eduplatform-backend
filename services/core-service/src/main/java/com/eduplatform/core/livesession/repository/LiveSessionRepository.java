package com.eduplatform.core.livesession.repository;

import com.eduplatform.core.livesession.model.LiveSession;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LiveSessionRepository extends MongoRepository<LiveSession, String> {

    Optional<LiveSession> findByRoomIdAndTenantId(String roomId, String tenantId);

    Optional<LiveSession> findByIdAndTenantId(String id, String tenantId);

    List<LiveSession> findByCourseIdAndTenantId(String courseId, String tenantId);

    List<LiveSession> findByInstructorIdAndStatusAndTenantId(String instructorId, String status, String tenantId);

    List<LiveSession> findByScheduledAtBetweenAndStatusAndTenantId(LocalDateTime start, LocalDateTime end,
                                                                   String status, String tenantId);

    List<LiveSession> findByStatusAndIsPublicAndTenantId(String status, boolean isPublic, String tenantId);
}