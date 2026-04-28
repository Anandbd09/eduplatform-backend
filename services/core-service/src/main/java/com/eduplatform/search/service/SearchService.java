package com.eduplatform.search.service;

import com.eduplatform.core.user.model.User;
import com.eduplatform.core.user.repository.UserRepository;
import com.eduplatform.search.dto.SearchRequest;
import com.eduplatform.search.dto.SearchResponse;
import com.eduplatform.search.exception.SearchException;
import com.eduplatform.search.model.SearchFilter;
import com.eduplatform.search.model.SearchIndex;
import com.eduplatform.search.model.SearchQuery;
import com.eduplatform.search.repository.SearchHistoryRepository;
import com.eduplatform.search.repository.SearchRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SearchService {

    private static final int DEFAULT_SUGGESTION_LIMIT = 10;

    private final SearchRepository searchRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;

    public SearchService(
            SearchRepository searchRepository,
            SearchHistoryRepository searchHistoryRepository,
            UserRepository userRepository,
            MongoTemplate mongoTemplate
    ) {
        this.searchRepository = searchRepository;
        this.searchHistoryRepository = searchHistoryRepository;
        this.userRepository = userRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public Page<SearchResponse> search(SearchRequest request, String userId, String tenantId) {
        if (request == null) {
            throw SearchException.badRequest("Search request is required");
        }

        SearchFilter filter = request.getFilter();
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        String resolvedTenantId = resolveTenantId(tenantId);
        String normalizedQuery = normalizeQuery(request.getQuery());

        List<SearchIndex> matchingCourses = searchRepository.searchCourses(resolvedTenantId, normalizedQuery, filter)
                .stream()
                .filter(index -> matchesAdvancedFilters(index, filter))
                .sorted(buildComparator(filter))
                .collect(Collectors.toList());

        long offset = pageable.getOffset();
        int fromIndex = (int) Math.min(offset, matchingCourses.size());
        int toIndex = (int) Math.min(offset + pageable.getPageSize(), matchingCourses.size());
        List<SearchIndex> pageContent = matchingCourses.subList(fromIndex, toIndex);

        Map<String, String> instructorNames = buildInstructorNameMap(pageContent, resolvedTenantId);
        List<SearchResponse> responses = pageContent.stream()
                .map(index -> convertToResponse(index, instructorNames))
                .collect(Collectors.toList());

        if (StringUtils.hasText(normalizedQuery)) {
            logSearchQuery(userId, resolvedTenantId, normalizedQuery, matchingCourses.size());
        }

        return new PageImpl<>(responses, pageable, matchingCourses.size());
    }

    public List<String> autocomplete(String query, String tenantId) {
        return searchRepository.autocompleteTitles(resolveTenantId(tenantId), query, DEFAULT_SUGGESTION_LIMIT);
    }

    public List<String> getSuggestions(String prefix, String tenantId) {
        return autocomplete(prefix, tenantId);
    }

    public List<String> getPopularSearches(String tenantId) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(
                        Criteria.where("tenantId").is(resolveTenantId(tenantId))
                                .and("query").ne(null).ne("")
                ),
                Aggregation.group("query").count().as("total"),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "total").and(Sort.by(Sort.Direction.ASC, "_id"))),
                Aggregation.limit(DEFAULT_SUGGESTION_LIMIT),
                Aggregation.project("total").and("_id").as("query")
        );

        AggregationResults<PopularSearchResult> results = mongoTemplate.aggregate(
                aggregation,
                "search_queries",
                PopularSearchResult.class
        );

        return results.getMappedResults().stream()
                .map(PopularSearchResult::getQuery)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }

    @Transactional
    public void indexCourse(String courseId) {
        log.debug("Search refresh requested for course: {}", courseId);
    }

    @Transactional
    public void removeFromIndex(String courseId) {
        log.debug("Search removal requested for course: {}", courseId);
    }

    @Transactional
    public void bulkIndex(List<SearchIndex> courses) {
        log.debug("Bulk search refresh requested for {} courses", courses != null ? courses.size() : 0);
    }

    private boolean matchesAdvancedFilters(SearchIndex index, SearchFilter filter) {
        if (filter == null) {
            return true;
        }

        double effectivePrice = resolveEffectivePrice(index);
        if (filter.getMinPrice() != null && effectivePrice < filter.getMinPrice()) {
            return false;
        }

        if (filter.getMaxPrice() != null && effectivePrice > filter.getMaxPrice()) {
            return false;
        }

        if (filter.getMaxDuration() != null
                && index.getDurationHours() != null
                && index.getDurationHours() > filter.getMaxDuration()) {
            return false;
        }

        return true;
    }

    private Comparator<SearchIndex> buildComparator(SearchFilter filter) {
        String sortBy = filter != null && StringUtils.hasText(filter.getSortBy())
                ? filter.getSortBy().trim().toLowerCase(Locale.ROOT)
                : "newest";

        Comparator<SearchIndex> newestComparator = Comparator
                .comparing(SearchIndex::getPublishedAt, Comparator.nullsLast(LocalDateTime::compareTo))
                .thenComparing(SearchIndex::getCreatedAt, Comparator.nullsLast(LocalDateTime::compareTo))
                .reversed();

        return switch (sortBy) {
            case "rating" -> Comparator
                    .comparing(SearchIndex::getRating, Comparator.nullsLast(Float::compareTo))
                    .reversed()
                    .thenComparing(newestComparator);
            case "popularity" -> Comparator
                    .comparing(SearchIndex::getEnrollmentCount, Comparator.nullsLast(Integer::compareTo))
                    .reversed()
                    .thenComparing(newestComparator);
            case "price_asc" -> Comparator
                    .comparing(this::resolveEffectivePrice, Comparator.nullsLast(Double::compareTo))
                    .thenComparing(newestComparator);
            case "price_desc" -> Comparator
                    .comparing(this::resolveEffectivePrice, Comparator.nullsLast(Double::compareTo))
                    .reversed()
                    .thenComparing(newestComparator);
            default -> newestComparator;
        };
    }

    private void logSearchQuery(String userId, String tenantId, String query, int resultCount) {
        searchHistoryRepository.save(
                SearchQuery.builder()
                        .id(UUID.randomUUID().toString())
                        .userId(userId)
                        .tenantId(resolveTenantId(tenantId))
                        .query(query)
                        .resultCount(resultCount)
                        .searchedAt(LocalDateTime.now())
                        .build()
        );
    }

    private SearchResponse convertToResponse(SearchIndex index, Map<String, String> instructorNames) {
        return SearchResponse.builder()
                .courseId(index.getCourseId())
                .title(index.getTitle())
                .description(index.getDescription())
                .instructorId(index.getInstructorId())
                .instructorName(instructorNames.getOrDefault(index.getInstructorId(), index.getInstructorId()))
                .category(index.getCategory())
                .level(index.getLevel())
                .thumbnailUrl(index.getThumbnailUrl())
                .pricingType(index.getPricingType())
                .price(index.getPrice())
                .discountPrice(index.getDiscountPrice())
                .effectivePrice(resolveEffectivePrice(index))
                .rating(index.getRating())
                .enrollmentCount(index.getEnrollmentCount())
                .tags(index.getTags() != null ? index.getTags() : new ArrayList<>())
                .durationHours(index.getDurationHours())
                .language(index.getLanguage())
                .publishedAt(index.getPublishedAt())
                .build();
    }

    private Map<String, String> buildInstructorNameMap(List<SearchIndex> indexes, String tenantId) {
        Map<String, String> instructorNames = new HashMap<>();

        for (SearchIndex index : indexes) {
            if (!StringUtils.hasText(index.getInstructorId()) || instructorNames.containsKey(index.getInstructorId())) {
                continue;
            }

            instructorNames.put(index.getInstructorId(), resolveInstructorName(index.getInstructorId(), tenantId));
        }

        return instructorNames;
    }

    private String resolveInstructorName(String instructorId, String tenantId) {
        return userRepository.findByIdAndTenantId(instructorId, tenantId)
                .or(() -> userRepository.findById(instructorId))
                .map(this::formatUserName)
                .filter(StringUtils::hasText)
                .orElse(instructorId);
    }

    private String formatUserName(User user) {
        String firstName = user.getFirstName() != null ? user.getFirstName().trim() : "";
        String lastName = user.getLastName() != null ? user.getLastName().trim() : "";
        String fullName = (firstName + " " + lastName).trim();
        return StringUtils.hasText(fullName) ? fullName : user.getEmail();
    }

    private Double resolveEffectivePrice(SearchIndex index) {
        if (index.getDiscountPrice() != null && index.getDiscountPrice() >= 0) {
            return index.getDiscountPrice();
        }

        return index.getPrice() != null ? index.getPrice() : 0D;
    }

    private String normalizeQuery(String query) {
        return StringUtils.hasText(query) ? query.trim() : null;
    }

    private String resolveTenantId(String tenantId) {
        return StringUtils.hasText(tenantId) ? tenantId : "default";
    }

    private static class PopularSearchResult {
        private String query;

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }
    }
}
