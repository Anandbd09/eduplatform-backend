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
@Document(collection = "forum_threads")
public class ForumThread {

    @Id
    private String id;

    @Indexed
    private String courseId;

    @Indexed
    private String creatorId;

    private String title;

    private String description;

    @Indexed
    private String category; // GENERAL, ASSIGNMENTS, RESOURCES, QUESTIONS

    @Indexed
    private String status; // ACTIVE, PINNED, LOCKED, ARCHIVED

    @Indexed
    private LocalDateTime createdAt;

    private LocalDateTime lastActivityAt;

    private Long viewCount;

    private Long replyCount;

    private Boolean isPinned;

    private Boolean isLocked;

    @Indexed
    private String tenantId;

    private Long version_field = 0L;
}