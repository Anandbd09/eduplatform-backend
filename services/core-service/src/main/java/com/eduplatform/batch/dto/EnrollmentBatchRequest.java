// FILE 30: EnrollmentBatchRequest.java
package com.eduplatform.batch.dto;
import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class EnrollmentBatchRequest {
    private List<String> userIds;
    private List<String> courseIds;
    private Boolean overwriteExisting;
    private Boolean sendWelcomeEmail;
}