// FILE 15: ExportJobRequest.java
package com.eduplatform.export.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ExportJobRequest {
    private String exportType; // USER_DATA, COURSE_DATA, PAYMENT_DATA, GDPR_DATA
    private String format; // CSV, PDF, EXCEL, JSON
    private String sourceEntity;
    private String filterCriteria; // JSON
    private String notificationEmail;
}