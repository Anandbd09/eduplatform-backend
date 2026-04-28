// FILE 23: ForumPostRequest.java
package com.eduplatform.social.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ForumPostRequest {
    private String threadId;
    private String content;
}