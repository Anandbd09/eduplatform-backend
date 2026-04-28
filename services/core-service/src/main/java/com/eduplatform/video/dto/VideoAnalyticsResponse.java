// FILE 29: VideoAnalyticsResponse.java
package com.eduplatform.video.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class VideoAnalyticsResponse {
    private String videoId;
    private Long totalViews;
    private Long uniqueViewers;
    private Double avgWatchTime;
    private Double completionRate;
    private Double engagementScore;
    private Long quality360Views;
    private Long quality720Views;
    private Long quality1080Views;
}