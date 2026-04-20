package com.eduplatform.core.enrollment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgressResponse {

    private String enrollmentId;

    private double progressPercentage;

    private int completedLessonsCount;

    private int totalWatchTimeSeconds;

    private LocalDateTime updatedAt;
}