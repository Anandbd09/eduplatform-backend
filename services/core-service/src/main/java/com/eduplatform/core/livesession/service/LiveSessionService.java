package com.eduplatform.core.livesession.service;

import com.eduplatform.core.livesession.dto.CreateSessionRequest;
import com.eduplatform.core.livesession.dto.SessionResponse;
import com.eduplatform.core.livesession.model.LiveSession;
import com.eduplatform.core.livesession.repository.LiveSessionRepository;
import com.eduplatform.core.common.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LiveSessionService {

    @Autowired
    private LiveSessionRepository liveSessionRepository;

    public SessionResponse createSession(CreateSessionRequest request, String instructorId, String tenantId) {

        String roomId = UUID.randomUUID().toString().substring(0, 8);

        LiveSession session = LiveSession.builder()
                .tenantId(tenantId)
                .courseId(request.getCourseId())
                .instructorId(instructorId)
                .roomId(roomId)
                .title(request.getTitle())
                .description(request.getDescription())
                .scheduledAt(request.getScheduledAt())
                .status("SCHEDULED")
                .maxParticipants(request.getMaxParticipants() != 0 ? request.getMaxParticipants() : 100)
                .isChatEnabled(true)
                .isPublic(request.isPublic())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        session = liveSessionRepository.save(session);

        log.info("Live session created: {} in room: {}", session.getId(), roomId);

        return mapToSessionResponse(session);
    }

    public SessionResponse startSession(String sessionId, String instructorId, String tenantId) {

        LiveSession session = liveSessionRepository.findByIdAndTenantId(sessionId, tenantId)
                .orElseThrow(() -> AppException.notFound("Session not found"));

        if (!session.getInstructorId().equals(instructorId)) {
            throw AppException.unauthorized("Only instructor can start this session");
        }

        session.setStatus("LIVE");
        session.setStartedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());

        session = liveSessionRepository.save(session);

        log.info("Session started: {}", sessionId);

        return mapToSessionResponse(session);
    }

    public SessionResponse endSession(String sessionId, String instructorId, String tenantId) {

        LiveSession session = liveSessionRepository.findByIdAndTenantId(sessionId, tenantId)
                .orElseThrow(() -> AppException.notFound("Session not found"));

        if (!session.getInstructorId().equals(instructorId)) {
            throw AppException.unauthorized("Only instructor can end this session");
        }

        session.setStatus("ENDED");
        session.setEndedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());

        session = liveSessionRepository.save(session);

        log.info("Session ended: {}", sessionId);

        return mapToSessionResponse(session);
    }

    public SessionResponse getSession(String sessionId, String tenantId) {

        LiveSession session = liveSessionRepository.findByIdAndTenantId(sessionId, tenantId)
                .orElseThrow(() -> AppException.notFound("Session not found"));

        return mapToSessionResponse(session);
    }

    public SessionResponse getSessionByRoomId(String roomId, String tenantId) {

        LiveSession session = liveSessionRepository.findByRoomIdAndTenantId(roomId, tenantId)
                .orElseThrow(() -> AppException.notFound("Session not found"));

        return mapToSessionResponse(session);
    }

    public List<SessionResponse> getCourseSessions(String courseId, String tenantId) {

        return liveSessionRepository.findByCourseIdAndTenantId(courseId, tenantId)
                .stream()
                .map(this::mapToSessionResponse)
                .collect(Collectors.toList());
    }

    public List<SessionResponse> getPublicLiveSessions(String tenantId) {

        return liveSessionRepository.findByStatusAndIsPublicAndTenantId("LIVE", true, tenantId)
                .stream()
                .map(this::mapToSessionResponse)
                .collect(Collectors.toList());
    }

    private SessionResponse mapToSessionResponse(LiveSession session) {
        return SessionResponse.builder()
                .id(session.getId())
                .roomId(session.getRoomId())
                .courseId(session.getCourseId())
                .title(session.getTitle())
                .description(session.getDescription())
                .scheduledAt(session.getScheduledAt())
                .startedAt(session.getStartedAt())
                .endedAt(session.getEndedAt())
                .status(session.getStatus())
                .participantCount(session.getParticipants().size())
                .maxParticipants(session.getMaxParticipants())
                .isChatEnabled(session.isChatEnabled())
                .isPublic(session.isPublic())
                .createdAt(session.getCreatedAt())
                .build();
    }
}