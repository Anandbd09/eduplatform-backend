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
@Document(collection = "certificate_badges")
public class CertificateBadge {

    @Id
    private String id;

    @Indexed
    private String certificateId;

    private String badgeUrl; // URL to badge image

    private String badgeName; // "Python Expert", "Azure Certified"

    private String badgeDescription;

    private String badgeCategory; // SKILL, ACHIEVEMENT, SPECIALIZATION

    @Indexed
    private Boolean isPublic; // Can be shared on LinkedIn, etc.

    @Indexed
    private String shareUrl; // Public share link

    @Indexed
    private LocalDateTime createdAt;

    @Indexed
    private String tenantId;

    private Long version = 0L;
}