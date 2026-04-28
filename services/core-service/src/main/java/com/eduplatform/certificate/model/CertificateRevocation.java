package com.eduplatform.certificate.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "certificate_revocations")
public class CertificateRevocation {

    @Id
    private String id;

    @Indexed
    private String certificateId;

    @Indexed
    private String certificateNumber;

    @Indexed
    private String userId;

    @Indexed
    private String courseId;

    @Indexed
    private String reason; // FRAUD, PLAGIARISM, INCORRECT_INFO, REQUEST

    private String details;

    @Indexed
    private LocalDateTime revokedAt;

    @Indexed
    private String revokedBy;

    private String revokedByName;

    @Indexed
    private String status; // ACTIVE, REVERSED, PENDING

    private String reversalReason;

    @Indexed
    private LocalDateTime reversedAt;

    @Indexed
    private LocalDateTime createdAt;

    @Indexed
    private String tenantId;

    private Long version = 0L;
}