package com.eduplatform.reporting.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;
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
@Document(collection = "reports")
@CompoundIndex(name = "reportedEntity_reporter_idx",
        def = "{'reportedEntityId': 1, 'reportedEntityType': 1, 'reporterId': 1, 'tenantId': 1}",
        unique = true)
public class Report {

    @Id
    private String id;

    @Indexed
    private String reportedEntityId; // course/user/review ID

    @Indexed
    private String reportedEntityType; // COURSE, USER, REVIEW, COMMENT

    @Indexed
    private String reporterId;

    private String reporterName;

    private String reporterEmail;

    @Indexed
    private String category; // PLAGIARISM, FRAUD, INAPPROPRIATE, SPAM, COPYRIGHT, OTHER

    private String description;

    @Indexed
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL

    private List<String> evidenceUrls; // File URLs (max 5, 10MB total)

    @Indexed
    private String status; // OPEN, UNDER_REVIEW, RESOLVED, DISMISSED

    private String resolutionNotes;

    @Indexed
    private LocalDateTime createdAt;

    @Indexed
    private LocalDateTime updatedAt;

    private LocalDateTime resolvedAt;

    @Indexed
    private String tenantId;

    private Long version = 0L;

    /**
     * Validate report before creation
     */
    public void validate() {
        if (reportedEntityId == null || reportedEntityId.isEmpty()) {
            throw new IllegalArgumentException("Entity ID required");
        }
        if (reportedEntityType == null || reportedEntityType.isEmpty()) {
            throw new IllegalArgumentException("Entity type required");
        }
        if (reporterId == null || reporterId.isEmpty()) {
            throw new IllegalArgumentException("Reporter ID required");
        }
        if (category == null || category.isEmpty()) {
            throw new IllegalArgumentException("Category required");
        }
        if (description == null || description.length() < 10) {
            throw new IllegalArgumentException("Description must be at least 10 characters");
        }
        if (evidenceUrls != null && evidenceUrls.size() > 5) {
            throw new IllegalArgumentException("Maximum 5 evidence files allowed");
        }
    }
}