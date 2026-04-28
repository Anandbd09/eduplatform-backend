// FILE 21: ForumThreadRequest.java
package com.eduplatform.social.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ForumThreadRequest {
    private String courseId;
    private String title;
    private String description;
    private String category;
}