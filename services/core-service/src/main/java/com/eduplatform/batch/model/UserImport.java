package com.eduplatform.batch.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "user_imports")
public class UserImport {

    @Id
    private String id;

    @Indexed
    private String jobId;

    @Indexed
    private String importType; // STUDENTS, INSTRUCTORS, ADMINS

    private List<String> columnHeaders;

    private Integer totalRows;

    private Integer importedRows;

    private Integer duplicateRows;

    private Integer invalidRows;

    @Indexed
    private String status; // QUEUED, PROCESSING, COMPLETED, FAILED

    private String duplicateHandling; // SKIP, UPDATE, ERROR

    private String invalidHandling; // SKIP, ERROR

    @Indexed
    private LocalDateTime createdAt;

    @Indexed
    private LocalDateTime completedAt;

    @Indexed
    private String tenantId;

    private Long version = 0L;
}