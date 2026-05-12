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
public class EnrolledStudentResponse {

    private String enrollmentId;

    private String studentId;

    private String firstName;

    private String lastName;

    private String email;

    private String status;

    private double progressPercentage;

    private int completedLessonsCount;

    private LocalDateTime enrolledAt;

    private LocalDateTime completedAt;
}
