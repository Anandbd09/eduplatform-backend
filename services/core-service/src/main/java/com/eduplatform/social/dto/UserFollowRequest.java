// FILE 17: UserFollowRequest.java
package com.eduplatform.social.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UserFollowRequest {
    private String followingId;
    private Boolean isPublic;
    private Boolean notificationsEnabled;
}