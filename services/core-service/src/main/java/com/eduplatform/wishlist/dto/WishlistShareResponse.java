// FILE 20: WishlistShareResponse.java
package com.eduplatform.wishlist.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistShareResponse {
    private String id;
    private String collectionId;
    private String sharedBy;
    private String sharedWith;
    private String shareToken;
    private LocalDateTime sharedAt;
    private LocalDateTime expiresAt;
    private Boolean isActive;
    private Boolean isExpired;
}