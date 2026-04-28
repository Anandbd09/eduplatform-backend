// FILE 19: MessageRequest.java
package com.eduplatform.social.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class MessageRequest {
    private String recipientId;
    private String subject;
    private String content;
}