package com.eduplatform.core.course.model;

import com.eduplatform.core.media.model.MediaAsset;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    private String id;

    @Indexed
    private String tenantId;

    @Indexed
    private String instructorId;

    private String title;

    private String description;

    private String category;

    private String level;  // BEGINNER, INTERMEDIATE, ADVANCED

    private String status;  // DRAFT, PENDING_APPROVAL, PUBLISHED, REJECTED, REMOVED, ARCHIVED

    @Builder.Default
    private List<Module> modules = new ArrayList<>();

    private String pricingType; // FREE, PAID

    private BigDecimal price;

    private BigDecimal discountPrice;

    private int enrolledCount;

    private double rating;  // 0-5

    private String thumbnailUrl;

    private MediaAsset thumbnailAsset;

    @Builder.Default
    private boolean certificateEnabled = false;

    private String language;

    @Builder.Default
    private List<String> tags = new ArrayList<>();

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime publishedAt;

    private LocalDateTime approvedAt;

    private String approvedBy;

    private String approvalNotes;

    private LocalDateTime rejectedAt;

    private String rejectedBy;

    private String rejectionReason;

    private LocalDateTime removedAt;

    private String removedBy;

    private String removalReason;
}
