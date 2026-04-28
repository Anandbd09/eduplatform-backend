package com.eduplatform.review.model;

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
@Document(collection = "reviews")
@CompoundIndex(name = "courseId_userId_idx", def = "{'courseId': 1, 'userId': 1, 'tenantId': 1}", unique = true)
public class Review {

    @Id
    private String id;

    @Indexed
    private String courseId;

    @Indexed
    private String userId;

    private String userName;
    private String userEmail;
    private String userAvatar;

    private Integer rating; // 1-5 stars
    private String title;
    private String content;

    private Integer helpfulCount = 0;
    private Integer unHelpfulCount = 0;

    private String status = "PENDING"; // PENDING, APPROVED, REJECTED
    private Boolean isVerifiedPurchase = true;

    private List<String> tags;

    @Indexed
    private LocalDateTime createdAt;

    @Indexed
    private LocalDateTime updatedAt;

    private LocalDateTime approvedAt;

    @Indexed
    private String tenantId;

    private Long version = 0L;

    /**
     * Check if review can be edited (within 30 days of creation)
     */
    public boolean canEdit(LocalDateTime now) {
        if (createdAt == null) {
            return false;
        }
        return now.isBefore(createdAt.plusDays(30));
    }

    /**
     * Check if review is expired (30 days old)
     */
    public boolean isExpired(LocalDateTime now) {
        return !canEdit(now);
    }

    /**
     * Get review age in days
     */
    public long getAgeInDays() {
        if (createdAt == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(createdAt, LocalDateTime.now());
    }
}