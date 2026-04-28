package com.eduplatform.search.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Data;
import java.util.List;

@Data
public class SearchFilter {
    private List<String> categories;
    private List<String> levels;

    @DecimalMin("0.0")
    private Double minPrice;

    @DecimalMin("0.0")
    private Double maxPrice;

    @DecimalMin("0.0")
    private Float minRating;

    @Min(0)
    private Integer minEnrollments;

    @Min(0)
    private Integer maxDuration;

    private List<String> tags;
    private String sortBy; // rating, popularity, newest, price_asc, price_desc
    private String status;
}
