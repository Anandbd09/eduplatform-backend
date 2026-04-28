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
@Document(collection = "video_transcodes")
public class VideoTranscode {

    @Id
    private String id;

    @Indexed(unique = true)
    private String videoId;

    @Indexed
    private String status; // QUEUED, PROCESSING, COMPLETED, FAILED

    private String quality; // 360, 720, 1080

    private String s3OutputUrl;

    private Long estimatedDurationSeconds;

    private Double progressPercentage;

    private String errorMessage;

    @Indexed
    private LocalDateTime startedAt;

    @Indexed
    private LocalDateTime completedAt;

    @Indexed
    private LocalDateTime createdAt;

    @Indexed
    private String tenantId;

    private Long version = 0L;
}