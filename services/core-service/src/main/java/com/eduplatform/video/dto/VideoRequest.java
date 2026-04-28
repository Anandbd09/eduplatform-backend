// FILE 20: VideoRequest.java
package com.eduplatform.video.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class VideoRequest {
    private String title;
    private String description;
    private String courseId;
    private String chapterId;
    private String fileName;
    private Boolean allowDownload;
    private Boolean requiresAuth;
}