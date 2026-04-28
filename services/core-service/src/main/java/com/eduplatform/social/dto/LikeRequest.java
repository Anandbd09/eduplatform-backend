// FILE 25: LikeRequest.java
package com.eduplatform.social.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LikeRequest {
    private String contentType; // POST, THREAD, MESSAGE
    private String contentId;
}