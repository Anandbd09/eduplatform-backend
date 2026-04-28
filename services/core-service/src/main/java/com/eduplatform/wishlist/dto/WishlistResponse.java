// FILE 14: WishlistResponse.java
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
public class WishlistResponse {
    private String id;
    private String userId;
    private String userName;
    private Integer totalItems;
    private Integer totalCollections;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}