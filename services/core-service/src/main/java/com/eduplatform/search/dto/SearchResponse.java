package com.eduplatform.search.dto;

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
public class SearchResponse {
    private String courseId;
    private String title;
    private String description;
    private String instructorId;
    private String instructorName;
    private String category;
    private String level;
    private String thumbnailUrl;
    private String pricingType;
    private Double discountPrice;
    private Double effectivePrice;
    private Float rating;
    private Double price;
    private Integer enrollmentCount;

    @Builder.Default
    private List<String> tags = new ArrayList<>();

    private Integer durationHours;
    private String language;
    private LocalDateTime publishedAt;
}
