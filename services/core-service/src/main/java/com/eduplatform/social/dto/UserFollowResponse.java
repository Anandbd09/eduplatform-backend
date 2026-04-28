// FILE 18: UserFollowResponse.java
package com.eduplatform.social.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UserFollowResponse {
    private String id;
    private String followerId;
    private String followingId;
    private String status;
    private LocalDateTime followedAt;
}