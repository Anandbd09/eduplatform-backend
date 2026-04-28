// FILE 23: UserImportResponse.java
package com.eduplatform.batch.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UserImportResponse {
    private String jobId;
    private String importType;
    private Integer totalRows;
    private Integer importedRows;
    private Integer duplicateRows;
    private Integer invalidRows;
    private String status;
    private LocalDateTime createdAt;
}