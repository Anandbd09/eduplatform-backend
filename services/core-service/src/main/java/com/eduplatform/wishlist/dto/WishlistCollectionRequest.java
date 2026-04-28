// FILE 17: WishlistCollectionRequest.java
package com.eduplatform.wishlist.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistCollectionRequest {
    private String name;
    private String description;
    private String visibility; // PRIVATE, SHARED, PUBLIC
}