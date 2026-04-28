// FILE 28: TranscodeResponse.java
package com.eduplatform.video.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TranscodeResponse {
    private String videoId;
    private String quality;
    private String status;
    private Double progressPercentage;
    private String outputUrl;
    private LocalDateTime completedAt;
}