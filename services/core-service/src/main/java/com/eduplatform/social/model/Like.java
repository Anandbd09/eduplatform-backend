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
@Document(collection = "likes")
public class Like {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String contentType; // POST, THREAD, MESSAGE, COMMENT

    @Indexed
    private String contentId; // The ID of what's being liked

    @Indexed(unique = true)
    private String likeKey; // "userId:contentId"

    @Indexed
    private LocalDateTime likedAt;

    @Indexed
    private String tenantId;

    private Long version_field = 0L;
}