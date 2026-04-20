package com.eduplatform.core.course.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Module {
    private String id;
    private String title;
    private String description;
    private int sequenceNumber;

    @Builder.Default
    private List<Lesson> lessons = new ArrayList<>();

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
