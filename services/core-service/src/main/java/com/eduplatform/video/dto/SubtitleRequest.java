// FILE 26: SubtitleRequest.java
package com.eduplatform.video.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SubtitleRequest {
    private String language;
    private String fileUrl;
}