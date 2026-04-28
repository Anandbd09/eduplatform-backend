package com.eduplatform.search.controller;

import com.eduplatform.core.common.response.ApiResponse;
import com.eduplatform.core.common.security.RequestContext;
import com.eduplatform.search.dto.FilterCriteria;
import com.eduplatform.search.dto.SearchResponse;
import com.eduplatform.search.service.SearchService;
import com.eduplatform.search.service.SearchFilterService;
import com.eduplatform.search.dto.SearchRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @Autowired
    private RequestContext requestContext;

    @Autowired
    private SearchFilterService filterService;

    @PostMapping
    public ResponseEntity<ApiResponse<Page<SearchResponse>>> search(
            @Valid @RequestBody SearchRequest request) {
        Page<SearchResponse> results = searchService.search(
                request,
                requestContext.getUserId(),
                requestContext.getTenantId()
        );

        return ResponseEntity.ok(ApiResponse.success(results, "Search completed successfully"));
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<ApiResponse<List<String>>> autocomplete(@RequestParam String query) {
        List<String> suggestions = searchService.autocomplete(query, requestContext.getTenantId());
        return ResponseEntity.ok(ApiResponse.success(suggestions, "Suggestions retrieved successfully"));
    }

    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<String>>> getPopularSearches() {
        List<String> popular = searchService.getPopularSearches(requestContext.getTenantId());
        return ResponseEntity.ok(ApiResponse.success(popular, "Popular searches retrieved successfully"));
    }

    @GetMapping("/filters")
    public ResponseEntity<ApiResponse<FilterCriteria>> getAvailableFilters() {
        FilterCriteria filters = filterService.getAvailableFilters(requestContext.getTenantId());
        return ResponseEntity.ok(ApiResponse.success(filters, "Available filters retrieved successfully"));
    }
}
