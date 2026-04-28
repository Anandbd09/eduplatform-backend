package com.eduplatform.wishlist.model;

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
@Document(collection = "wishlist_shares")
public class WishlistShare {

    @Id
    private String id;

    @Indexed
    private String collectionId;

    @Indexed
    private String sharedBy; // userId who created the share

    @Indexed
    private String sharedWith; // userId who received the share

    private String shareToken;

    @Indexed
    private LocalDateTime sharedAt;

    @Indexed
    private LocalDateTime expiresAt;

    private Boolean isActive = true;

    private String tenantId;

    /**
     * Check if share is expired
     */
    public boolean isExpired() {
        return this.expiresAt != null && LocalDateTime.now().isAfter(this.expiresAt);
    }
}
