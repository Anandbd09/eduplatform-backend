// FILE 21: VideoResponse.java
package com.eduplatform.video.dto;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class VideoResponse {
    private String videoId;
    private String title;
    private String description;
    private String courseId;
    private String instructorId;
    private Integer duration;
    private String status;
    private Boolean isPublished;
    private Boolean isReady;
    private String hlsUrl;
    private String dashUrl;
    private List<String> subtitleLanguages;
    private Long viewCount;
    private LocalDateTime uploadedAt;
    private LocalDateTime publishedAt;
}