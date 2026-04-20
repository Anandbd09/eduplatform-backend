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
public class EnrollmentResponse {

    private String id;

    private String userId;

    private String courseId;

    private String status;

    private double progressPercentage;

    private int completedLessonsCount;

    private LocalDateTime enrolledAt;

    private LocalDateTime completedAt;
}