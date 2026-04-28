// FILE 23: PlaybackRequest.java
package com.eduplatform.video.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PlaybackRequest {
    private String courseId;
    private Long currentPositionSeconds;
    private Long watchedSeconds;
    private String deviceType;
    private String quality;
    private Double playbackSpeed;
}