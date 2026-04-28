// FILE 16: ReportRequest.java
package com.eduplatform.reporting.dto;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportRequest {
    private String reportedEntityId;
    private String reportedEntityType;
    private String reporterName;
    private String reporterEmail;
    private String category;
    private String description;
    private String severity;
    private List<String> evidenceUrls;

    public void validate() {
        if (description == null || description.length() < 10) {
            throw new IllegalArgumentException("Description must be at least 10 characters");
        }
    }
}