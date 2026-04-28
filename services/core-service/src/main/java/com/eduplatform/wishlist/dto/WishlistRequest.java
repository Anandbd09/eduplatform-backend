// FILE 13: WishlistRequest.java
package com.eduplatform.wishlist.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistRequest {
    private String userId;
}