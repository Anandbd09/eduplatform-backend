// FILE 20: MessageResponse.java
package com.eduplatform.social.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class MessageResponse {
    private String id;
    private String senderId;
    private String recipientId;
    private String subject;
    private String status;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
}