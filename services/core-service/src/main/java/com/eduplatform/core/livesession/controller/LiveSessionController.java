package com.eduplatform.core.livesession.controller;

import com.eduplatform.core.livesession.dto.CreateSessionRequest;
import com.eduplatform.core.livesession.dto.SessionResponse;
import com.eduplatform.core.livesession.service.LiveSessionService;
import com.eduplatform.core.common.response.ApiResponse;
import com.eduplatform.core.common.security.RequestContext;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/live")
public class LiveSessionController {

    @Autowired
    private LiveSessionService liveSessionService;

    @Autowired
    private RequestContext requestContext;

    @PostMapping("/sessions")
    public ResponseEntity<ApiResponse<SessionResponse>> createSession(
            @Valid @RequestBody CreateSessionRequest request) {

        log.info("Create live session for course: {}", request.getCourseId());

        SessionResponse response = liveSessionService.createSession(
                request,
                requestContext.getUserId(),
                requestContext.getTenantId()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Live session created"));
    }

    @PostMapping("/sessions/{sessionId}/start")
    public ResponseEntity<ApiResponse<SessionResponse>> startSession(
            @PathVariable String sessionId) {

        log.info("Start session: {}", sessionId);

        SessionResponse response = liveSessionService.startSession(
                sessionId,
                requestContext.getUserId(),
                requestContext.getTenantId()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response, "Session started"));
    }

    @PostMapping("/sessions/{sessionId}/end")
    public ResponseEntity<ApiResponse<SessionResponse>> endSession(
            @PathVariable String sessionId) {

        log.info("End session: {}", sessionId);

        SessionResponse response = liveSessionService.endSession(
                sessionId,
                requestContext.getUserId(),
                requestContext.getTenantId()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response, "Session ended"));
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<ApiResponse<SessionResponse>> getSession(
            @PathVariable String sessionId) {

        SessionResponse response = liveSessionService.getSession(
                sessionId,
                requestContext.getTenantId()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response, "Session retrieved"));
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<ApiResponse<SessionResponse>> getSessionByRoom(
            @PathVariable String roomId) {

        SessionResponse response = liveSessionService.getSessionByRoomId(
                roomId,
                requestContext.getTenantId()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response, "Session retrieved"));
    }

    @GetMapping("/courses/{courseId}/sessions")
    public ResponseEntity<ApiResponse<List<SessionResponse>>> getCourseSessions(
            @PathVariable String courseId) {

        List<SessionResponse> response = liveSessionService.getCourseSessions(
                courseId,
                requestContext.getTenantId()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response, "Sessions retrieved"));
    }

    @GetMapping("/public/live")
    public ResponseEntity<ApiResponse<List<SessionResponse>>> getPublicLiveSessions() {

        List<SessionResponse> response = liveSessionService.getPublicLiveSessions(
                requestContext.getTenantId()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response, "Live sessions retrieved"));
    }
}