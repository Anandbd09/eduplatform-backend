package com.eduplatform.wishlist.model;

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
@Document(collection = "wishlist_items")
@CompoundIndex(name = "userId_courseId_idx", def = "{'userId': 1, 'courseId': 1, 'tenantId': 1}", unique = true)
public class WishlistItem {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String courseId;

    private String courseName;
    private String courseImage;
    private String courseDescription;
    private Double coursePrice;
    private Double courseRating;
    private String instructorName;
    private String instructorId;

    @Indexed
    private Boolean isFavorite = false;

    private List<String> collectionIds;

    @Indexed
    private LocalDateTime addedAt;

    @Indexed
    private LocalDateTime markedFavoriteAt;

    @Indexed
    private String tenantId;

    private Long version = 0L;

    /**
     * Mark as favorite
     */
    public void markAsFavorite() {
        this.isFavorite = true;
        this.markedFavoriteAt = LocalDateTime.now();
    }

    /**
     * Unmark as favorite
     */
    public void unmarkAsFavorite() {
        this.isFavorite = false;
        this.markedFavoriteAt = null;
    }

    /**
     * Add to collection
     */
    public void addToCollection(String collectionId) {
        if (this.collectionIds == null) {
            this.collectionIds = new java.util.ArrayList<>();
        }
        if (!this.collectionIds.contains(collectionId)) {
            this.collectionIds.add(collectionId);
        }
    }

    /**
     * Remove from collection
     */
    public void removeFromCollection(String collectionId) {
        if (this.collectionIds != null) {
            this.collectionIds.remove(collectionId);
        }
    }
}
