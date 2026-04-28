// FILE 22: ForumThreadResponse.java
package com.eduplatform.social.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ForumThreadResponse {
    private String id;
    private String courseId;
    private String title;
    private String category;
    private String status;
    private LocalDateTime createdAt;
    private Long replyCount;
}