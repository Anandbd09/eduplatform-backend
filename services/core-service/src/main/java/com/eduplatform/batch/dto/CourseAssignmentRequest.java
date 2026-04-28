// FILE 24: CourseAssignmentRequest.java
package com.eduplatform.batch.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CourseAssignmentRequest {
    private String courseId;
    private String filterCriteria; // JSON filter
    private Boolean sendNotification;
}