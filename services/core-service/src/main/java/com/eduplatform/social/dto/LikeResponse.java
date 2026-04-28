// FILE 26: LikeResponse.java
package com.eduplatform.social.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LikeResponse {
    private String id;
    private String userId;
    private String contentType;
    private String contentId;
    private LocalDateTime likedAt;
}