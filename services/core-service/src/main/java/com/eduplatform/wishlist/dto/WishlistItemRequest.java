// FILE 15: WishlistItemRequest.java
package com.eduplatform.wishlist.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistItemRequest {
    private String courseId;
    private String courseName;
}