// FILE 24: ForumPostResponse.java
package com.eduplatform.social.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ForumPostResponse {
    private String id;
    private String threadId;
    private String authorId;
    private String content;
    private LocalDateTime createdAt;
    private Long likeCount;
    private Boolean isAnswer;
}