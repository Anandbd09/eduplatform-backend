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
@Document(collection = "messages")
public class Message {

    @Id
    private String id;

    @Indexed
    private String senderId;

    @Indexed
    private String recipientId;

    private String subject;

    private String content;

    @Indexed
    private String status; // SENT, READ, ARCHIVED

    @Indexed
    private LocalDateTime sentAt;

    private LocalDateTime readAt;

    private LocalDateTime archivedAt;

    private Boolean isReply;

    private String replyToMessageId;

    private Integer attachmentCount;

    @Indexed
    private String tenantId;

    private Long version_field = 0L;

    /**
     * Is message read
     */
    public boolean isRead() {
        return "READ".equals(status);
    }
}