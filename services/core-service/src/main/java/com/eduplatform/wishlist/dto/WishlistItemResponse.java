// FILE 16: WishlistItemResponse.java
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
public class WishlistItemResponse {
    private String id;
    private String courseId;
    private String courseName;
    private String courseImage;
    private String courseDescription;
    private Double coursePrice;
    private Double courseRating;
    private String instructorName;
    private Boolean isFavorite;
    private LocalDateTime addedAt;
    private LocalDateTime markedFavoriteAt;
    private Integer collectionCount;
}