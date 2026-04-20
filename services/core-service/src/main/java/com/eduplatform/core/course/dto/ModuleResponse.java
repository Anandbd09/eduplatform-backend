package com.eduplatform.core.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ModuleResponse {
    private String id;

    private String title;

    private String description;

    private int sequenceNumber;

    private int lessonCount;

    private List<LessonResponse> lessons;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
