// FILE 24: ReportingAnalytics.java
package com.eduplatform.reporting.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportingAnalytics {
    private Integer totalReports;
    private Integer openReports;
    private Integer underReviewReports;
    private Integer resolvedReports;
    private Integer dismissedReports;
    private Double resolutionRate;
    private LocalDateTime timestamp;
}