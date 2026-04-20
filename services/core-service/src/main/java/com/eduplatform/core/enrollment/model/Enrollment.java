package com.eduplatform.core.enrollment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "enrollments")@CompoundIndexes({
        @CompoundIndex(name = "user_course_tenant_idx", def = "{'userId': 1, 'courseId': 1, 'tenantId': 1}", unique = true)
})
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Enrollment {

    @Id
    private String id;

    private String tenantId;

    private String userId;

    private String courseId;

    private String status; // ACTIVE, COMPLETED, PAUSED, DROPPED

    private double progressPercentage;

    @Builder.Default
    private List<CompletedLesson> completedLessons = new ArrayList<>();

    private LocalDateTime enrolledAt;

    private LocalDateTime completedAt;

    private LocalDateTime updatedAt;



    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CompletedLesson {

        private String lessonId;

        private String moduleId;

        private LocalDateTime completedAt;

        private int watchTimeSeconds;
    }

}
