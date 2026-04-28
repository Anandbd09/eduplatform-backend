// FILE 25: CourseAssignmentResponse.java
package com.eduplatform.batch.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CourseAssignmentResponse {
    private String jobId;
    private String courseId;
    private Integer totalUserCount;
    private Integer assignedCount;
    private Integer alreadyEnrolledCount;
    private String status;
    private LocalDateTime completedAt;
}