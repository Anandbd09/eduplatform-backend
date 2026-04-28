package com.eduplatform.search.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchIndex {
    private String id;
    private String courseId;
    private String title;
    private String description;
    private String content;
    private String instructorId;
    private String category;
    private String level;
    private String status;
    private String pricingType;
    private String thumbnailUrl;
    private String language;
    private Double price;
    private Double discountPrice;
    private Float rating;
    private Integer enrollmentCount;
    private Integer reviewCount;
    private Integer durationHours;
    @Builder.Default
    private List<String> tags = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    private String tenantId;
}
