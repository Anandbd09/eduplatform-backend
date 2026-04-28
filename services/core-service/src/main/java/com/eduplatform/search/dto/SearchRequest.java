package com.eduplatform.search.dto;

import com.eduplatform.search.model.SearchFilter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class SearchRequest {
    private String query;

    @Valid
    private SearchFilter filter;

    @Min(0)
    private int page = 0;

    @Min(1)
    @Max(100)
    private int size = 20;
}
