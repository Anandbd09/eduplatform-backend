// FILE 25: StreamingManifestResponse.java
package com.eduplatform.video.dto;
import lombok.*;
import java.util.Map;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class StreamingManifestResponse {
    private String videoId;
    private String hlsUrl;
    private String dashUrl;
    private String progressiveUrl;
    private Map<String, String> subtitles;
    private Integer duration;
}