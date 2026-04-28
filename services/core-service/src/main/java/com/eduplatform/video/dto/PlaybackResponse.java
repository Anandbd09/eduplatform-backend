// FILE 24: PlaybackResponse.java
package com.eduplatform.video.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PlaybackResponse {
    private String videoId;
    private String userId;
    private Long currentPositionSeconds;
    private Double completionPercentage;
    private Boolean isCompleted;
    private LocalDateTime lastPlayedAt;
}