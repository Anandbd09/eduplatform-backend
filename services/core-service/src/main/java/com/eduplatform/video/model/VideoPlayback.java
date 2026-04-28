package com.eduplatform.video.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "video_playbacks")
@CompoundIndex(name = "userId_videoId_idx", def = "{'userId': 1, 'videoId': 1, 'tenantId': 1}")
public class VideoPlayback {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String videoId;

    @Indexed
    private String courseId;

    private Long currentPositionSeconds;

    private Long totalWatchedSeconds;

    private Double completionPercentage; // 0-100

    private Integer playCount; // Number of times started

    @Indexed
    private LocalDateTime lastPlayedAt;

    @Indexed
    private LocalDateTime completedAt; // When user finished watching (>=90%)

    private String deviceType; // MOBILE, TABLET, DESKTOP

    private String playerQuality; // 360, 720, 1080, AUTO

    private Double playbackSpeed; // 0.5, 1.0, 1.5, 2.0

    @Indexed
    private LocalDateTime createdAt;

    @Indexed
    private String tenantId;

    private Long version = 0L;

    /**
     * Check if video is completed (>= 90%)
     */
    public boolean isCompleted() {
        return completionPercentage != null && completionPercentage >= 90.0;
    }
}