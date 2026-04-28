// FILE 17: CertificateRequest.java
package com.eduplatform.certificate.dto;
import lombok.*;
import java.util.List;
import java.util.Map;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CertificateRequest {
    private String userId;
    private String userName;
    private String userEmail;
    private String courseId;
    private String courseName;
    private String templateId;
    private Map<String, String> customFields;
    private Double courseCompletion;
    private Integer totalLessons;
    private Integer completedLessons;
}