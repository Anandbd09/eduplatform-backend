package com.eduplatform.core.course.dto;

import com.eduplatform.core.media.model.MediaAsset;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CourseResponse {
    private String id;

    private String title;

    private String description;

    private String category;

    private String level;

    private String status;

    private String thumbnailUrl;

    private MediaAsset thumbnailAsset;

    private String pricingType;

    private BigDecimal price;

    private BigDecimal discountPrice;

    private int enrolledCount;

    private int moduleCount;

    private int lessonCount;

    private boolean certificateEnabled;

    private String language;

    private List<ModuleResponse> modules;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime publishedAt;
}
