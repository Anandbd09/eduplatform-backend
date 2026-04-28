package com.eduplatform.video.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "video_analytics")
public class VideoAnalytics {

    @Id
    private String id;

    @Indexed(unique = true)
    private String videoId;

    private Long totalViews;

    private Long uniqueViewers;

    private Double avgWatchTimeSeconds;

    private Double avgCompletionPercentage;

    private Long totalPlayCount;

    private Double engagementScore; // 0-100

    private Long quality360Views;
    private Long quality720Views;
    private Long quality1080Views;

    private String topCountry;

    private String topDeviceType;

    @Indexed
    private LocalDateTime lastUpdatedAt;

    @Indexed
    private LocalDateTime createdAt;

    @Indexed
    private String tenantId;

    private Long version = 0L;
}