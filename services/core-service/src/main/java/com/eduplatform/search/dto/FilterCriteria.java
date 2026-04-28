package com.eduplatform.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterCriteria {
    @Builder.Default
    private List<String> categories = new ArrayList<>();

    @Builder.Default
    private List<String> levels = new ArrayList<>();

    @Builder.Default
    private List<String> languages = new ArrayList<>();

    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Builder.Default
    private List<String> priceRanges = new ArrayList<>();

    @Builder.Default
    private List<String> ratings = new ArrayList<>();
}
