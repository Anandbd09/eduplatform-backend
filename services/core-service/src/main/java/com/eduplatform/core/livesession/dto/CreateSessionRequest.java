package com.eduplatform.core.livesession.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSessionRequest {

    @NotBlank(message = "Course ID is required")
    private String courseId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Scheduled time is required")
    private LocalDateTime scheduledAt;

    private int maxParticipants;

    private boolean isPublic;
}