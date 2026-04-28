package com.eduplatform.batch.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "course_assignments")
public class CourseAssignment {

    @Id
    private String id;

    @Indexed
    private String jobId;

    @Indexed
    private String courseId;

    private String courseTitle;

    private Integer totalUserCount;

    private Integer assignedCount;

    private Integer alreadyEnrolledCount;

    private Integer skippedCount;

    private Integer failedCount;

    @Indexed
    private String status; // QUEUED, PROCESSING, COMPLETED, FAILED

    private String filterCriteria; // JSON filter

    private Boolean sendNotification;

    @Indexed
    private LocalDateTime createdAt;

    @Indexed
    private LocalDateTime completedAt;

    @Indexed
    private String tenantId;

    private Long version = 0L;
}