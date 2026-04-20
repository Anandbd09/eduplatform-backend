package com.eduplatform.core.livesession.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionResponse {

    private String id;

    private String roomId;

    private String courseId;

    private String title;

    private String description;

    private LocalDateTime scheduledAt;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    private String status;

    private int participantCount;

    private int maxParticipants;

    private boolean isChatEnabled;

    private boolean isPublic;

    private LocalDateTime createdAt;
}