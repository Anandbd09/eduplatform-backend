package com.eduplatform.export.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "export_audit_logs")
public class ExportAuditLog {

    @Id
    private String id;

    @Indexed
    private String jobId;

    @Indexed
    private String userId;

    @Indexed
    private String action; // CREATED, STARTED, COMPLETED, FAILED, DOWNLOADED, DELETED

    private String details;

    private String ipAddress;

    private Integer recordsExported;

    @Indexed
    private LocalDateTime timestamp;

    @Indexed
    private String tenantId;

    private Long version_field = 0L;
}