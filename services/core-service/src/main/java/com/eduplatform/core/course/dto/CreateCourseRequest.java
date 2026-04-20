package com.eduplatform.core.course.dto;

import com.eduplatform.core.media.model.MediaAsset;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateCourseRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Category is required")
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

    @Builder.Default
    @Valid
    private List<CreateCourseModuleRequest> modules = new ArrayList<>();
}
