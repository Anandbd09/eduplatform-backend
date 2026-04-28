// FILE 22: VideoUploadResponse.java
package com.eduplatform.video.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class VideoUploadResponse {
    private String videoId;
    private String status;
    private String presignedUrl;
    private String bucket;
    private String key;
}