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
public class ParticipantResponse {

    private String userId;

    private String name;

    private String role;

    private boolean isMuted;

    private boolean isCameraOff;

    private LocalDateTime joinedAt;
}