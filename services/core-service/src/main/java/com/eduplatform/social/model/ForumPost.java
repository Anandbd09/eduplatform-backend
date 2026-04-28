package com.eduplatform.social.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "forum_posts")
public class ForumPost {

    @Id
    private String id;

    @Indexed
    private String threadId;

    @Indexed
    private String authorId;

    private String content;

    private Boolean isAnswer; // Is this marked as answer

    @Indexed
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Long likeCount;

    private Long replyCount;

    private String[] mentions; // @mentions parsed from content

    private Boolean isEdited;

    private Integer editCount;

    @Indexed
    private String tenantId;

    private Long version_field = 0L;
}