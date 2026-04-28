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
@Document(collection = "wishlists")
@CompoundIndex(name = "userId_tenantId_idx", def = "{'userId': 1, 'tenantId': 1}")
public class Wishlist {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String userName;
    private String userEmail;

    @Indexed
    private LocalDateTime createdAt;

    @Indexed
    private LocalDateTime updatedAt;

    private Integer totalItems = 0;
    private Integer totalCollections = 0;

    @Indexed
    private String tenantId;

    private Long version = 0L;

    /**
     * Increment total items
     */
    public void addItem() {
        this.totalItems = (this.totalItems == null ? 0 : this.totalItems) + 1;
    }

    /**
     * Decrement total items
     */
    public void removeItem() {
        this.totalItems = Math.max(0, (this.totalItems == null ? 0 : this.totalItems) - 1);
    }

    /**
     * Increment total collections
     */
    public void addCollection() {
        this.totalCollections = (this.totalCollections == null ? 0 : this.totalCollections) + 1;
    }

    /**
     * Decrement total collections
     */
    public void removeCollection() {
        this.totalCollections = Math.max(0, (this.totalCollections == null ? 0 : this.totalCollections) - 1);
    }
}
