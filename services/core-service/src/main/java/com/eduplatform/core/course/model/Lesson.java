package com.eduplatform.core.course.model;

import com.eduplatform.core.media.model.MediaAsset;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson {
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

    private String content;  // Markdown or HTML

    @Builder.Default
    private List<String> resources = new ArrayList<>();

    @Builder.Default
    private boolean freePreview = false;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
