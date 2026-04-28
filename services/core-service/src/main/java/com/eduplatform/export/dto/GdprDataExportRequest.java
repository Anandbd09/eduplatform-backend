// FILE 21: GdprDataExportRequest.java
package com.eduplatform.export.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class GdprDataExportRequest {
    private Boolean includePersonalData;
    private Boolean includeActivityLog;
    private Boolean includePaymentHistory;
}