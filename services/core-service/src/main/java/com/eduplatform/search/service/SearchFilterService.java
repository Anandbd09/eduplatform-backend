package com.eduplatform.search.service;

import com.eduplatform.search.dto.FilterCriteria;
import com.eduplatform.search.repository.SearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchFilterService {

    private final SearchRepository searchRepository;

    public FilterCriteria getAvailableFilters(String tenantId) {
        return FilterCriteria.builder()
                .categories(searchRepository.findDistinctValues(tenantId, "category"))
                .levels(searchRepository.findDistinctValues(tenantId, "level"))
                .languages(searchRepository.findDistinctValues(tenantId, "language"))
                .tags(searchRepository.findDistinctTags(tenantId))
                .priceRanges(getPriceRanges())
                .ratings(getRatings())
                .build();
    }

    private List<String> getPriceRanges() {
        return List.of("FREE", "0-100", "100-500", "500+");
    }

    private List<String> getRatings() {
        return List.of("4.5+", "4.0+", "3.5+", "3.0+");
    }
}
