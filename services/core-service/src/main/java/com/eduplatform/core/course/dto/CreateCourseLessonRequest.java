package com.eduplatform.core.course.dto;

import com.eduplatform.core.media.model.MediaAsset;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCourseLessonRequest {

    @NotBlank(message = "Lesson title is required")
    private String title;

    private String description;

    private String lessonType;

    private String videoUrl;

    private MediaAsset videoAsset;

    private String videoThumbnail;

    private MediaAsset thumbnailAsset;

    @Min(value = 0, message = "Duration must be zero or greater")
    private Integer durationSeconds;

    private Integer sequenceNumber;

    private String content;

    @Builder.Default
    private List<String> resources = new ArrayList<>();

    private Boolean freePreview;
}
