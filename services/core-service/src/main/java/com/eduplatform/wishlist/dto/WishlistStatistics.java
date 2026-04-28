// FILE 19: WishlistStatistics.java
package com.eduplatform.wishlist.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistStatistics {
    private Long totalItems;
    private Long favoriteItems;
    private Long nonFavoriteItems;
}