package com.eduplatform.core.course.dto;

import com.eduplatform.core.media.model.MediaAsset;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatchCourseRequest {

    private String title;

    private String description;

    private String category;

    private String level;

    private String thumbnailUrl;

    private MediaAsset thumbnailAsset;

    private String pricingType;

    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be zero or greater")
    private BigDecimal price;

    @DecimalMin(value = "0.0", inclusive = true, message = "Discount price must be zero or greater")
    private BigDecimal discountPrice;

    private Boolean certificateEnabled;

    private String language;
}
