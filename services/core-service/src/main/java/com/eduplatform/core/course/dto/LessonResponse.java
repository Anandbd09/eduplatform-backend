package com.eduplatform.core.course.dto;

import com.eduplatform.core.media.model.MediaAsset;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LessonResponse {

    private String id;

    private String title;

    private String description;

    private String lessonType;

    private String videoUrl;

    private MediaAsset videoAsset;

    private String videoThumbnail;

    private MediaAsset thumbnailAsset;

    private int durationSeconds;

    private int sequenceNumber;

    private String content;

    private List<String> resources;

    private boolean freePreview;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
