package com.eduplatform.certificate.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "certificate_templates")
public class CertificateTemplate {

    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    private String description;

    private String templateDesignUrl; // URL to template image/HTML

    @Indexed
    private String status; // ACTIVE, INACTIVE, ARCHIVED

    private String borderDesign;

    private String backgroundColor;

    private String fontFamily;

    private String signature; // Admin signature image URL

    private String seal; // Organization seal image URL

    private List<String> customFieldNames; // ["score", "grade", "instructor"]

    private String language; // en, hi, etc.

    @Indexed
    private LocalDateTime createdAt;

    @Indexed
    private LocalDateTime updatedAt;

    @Indexed
    private String createdBy;

    @Indexed
    private String tenantId;

    private Long version = 0L;
}