// FILE 18: WishlistCollectionResponse.java
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
public class WishlistCollectionResponse {
    private String id;
    private String userId;
    private String name;
    private String description;
    private String visibility;
    private Integer courseCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isPrivate;
    private Boolean isShared;
    private Boolean isPublic;
}