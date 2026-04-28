package com.eduplatform.search.repository;

import com.eduplatform.core.course.model.Course;
import com.eduplatform.core.course.model.Lesson;
import com.eduplatform.core.course.model.Module;
import com.eduplatform.search.model.SearchIndex;
import com.eduplatform.search.model.SearchFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class SearchRepository {

    private static final String DEFAULT_STATUS = "PUBLISHED";

    private final MongoTemplate mongoTemplate;

    public List<SearchIndex> searchCourses(String tenantId, String queryText, SearchFilter filter) {
        Query query = buildSearchQuery(tenantId, queryText, filter);
        query.with(Sort.by(Sort.Direction.DESC, "publishedAt", "createdAt"));

        return mongoTemplate.find(query, Course.class)
                .stream()
                .map(this::mapToSearchIndex)
                .collect(Collectors.toList());
    }

    public List<String> autocompleteTitles(String tenantId, String queryText, int limit) {
        if (!StringUtils.hasText(queryText)) {
            return List.of();
        }

        Query query = new Query();
        query.addCriteria(Criteria.where("tenantId").is(resolveTenantId(tenantId)));
        query.addCriteria(Criteria.where("status").is(DEFAULT_STATUS));
        query.addCriteria(Criteria.where("title").regex("^" + Pattern.quote(queryText.trim()), "i"));
        query.fields().include("title").exclude("_id");
        query.with(Sort.by(Sort.Direction.DESC, "enrolledCount").and(Sort.by(Sort.Direction.ASC, "title")));
        query.limit(Math.max(limit * 3, limit));

        return mongoTemplate.find(query, Course.class)
                .stream()
                .map(Course::getTitle)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<String> findDistinctValues(String tenantId, String fieldName) {
        Query query = new Query();
        query.addCriteria(Criteria.where("tenantId").is(resolveTenantId(tenantId)));
        query.addCriteria(Criteria.where("status").is(DEFAULT_STATUS));

        return mongoTemplate.findDistinct(query, fieldName, Course.class, String.class)
                .stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    public List<String> findDistinctTags(String tenantId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("tenantId").is(resolveTenantId(tenantId)));
        query.addCriteria(Criteria.where("status").is(DEFAULT_STATUS));
        query.fields().include("tags").exclude("_id");

        return mongoTemplate.find(query, Course.class)
                .stream()
                .map(Course::getTags)
                .filter(tags -> tags != null && !tags.isEmpty())
                .flatMap(List::stream)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    private Query buildSearchQuery(String tenantId, String queryText, SearchFilter filter) {
        Query query = new Query();
        query.addCriteria(Criteria.where("tenantId").is(resolveTenantId(tenantId)));
        query.addCriteria(Criteria.where("status").is(resolveStatus(filter)));

        if (filter != null) {
            if (filter.getCategories() != null && !filter.getCategories().isEmpty()) {
                query.addCriteria(Criteria.where("category").in(filter.getCategories()));
            }

            if (filter.getLevels() != null && !filter.getLevels().isEmpty()) {
                query.addCriteria(Criteria.where("level").in(filter.getLevels()));
            }

            if (filter.getMinRating() != null) {
                query.addCriteria(Criteria.where("rating").gte(filter.getMinRating()));
            }

            if (filter.getMinEnrollments() != null) {
                query.addCriteria(Criteria.where("enrolledCount").gte(filter.getMinEnrollments()));
            }

            if (filter.getTags() != null && !filter.getTags().isEmpty()) {
                query.addCriteria(Criteria.where("tags").in(filter.getTags()));
            }
        }

        if (StringUtils.hasText(queryText)) {
            String regex = Pattern.quote(queryText.trim());
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("title").regex(regex, "i"),
                    Criteria.where("description").regex(regex, "i"),
                    Criteria.where("category").regex(regex, "i"),
                    Criteria.where("tags").regex(regex, "i"),
                    Criteria.where("modules.title").regex(regex, "i"),
                    Criteria.where("modules.description").regex(regex, "i"),
                    Criteria.where("modules.lessons.title").regex(regex, "i"),
                    Criteria.where("modules.lessons.description").regex(regex, "i"),
                    Criteria.where("modules.lessons.content").regex(regex, "i")
            ));
        }

        return query;
    }

    private SearchIndex mapToSearchIndex(Course course) {
        int totalDurationSeconds = safeModules(course).stream()
                .flatMap(module -> safeLessons(module).stream())
                .mapToInt(Lesson::getDurationSeconds)
                .sum();

        return SearchIndex.builder()
                .id(course.getId())
                .courseId(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .content(buildContent(course))
                .instructorId(course.getInstructorId())
                .category(course.getCategory())
                .level(course.getLevel())
                .status(course.getStatus())
                .pricingType(course.getPricingType())
                .thumbnailUrl(course.getThumbnailUrl())
                .language(course.getLanguage())
                .price(course.getPrice() != null ? course.getPrice().doubleValue() : 0D)
                .discountPrice(course.getDiscountPrice() != null ? course.getDiscountPrice().doubleValue() : null)
                .rating((float) course.getRating())
                .enrollmentCount(course.getEnrolledCount())
                .reviewCount(0)
                .durationHours((int) Math.ceil(totalDurationSeconds / 3600.0))
                .tags(course.getTags() != null ? new ArrayList<>(course.getTags()) : new ArrayList<>())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .publishedAt(course.getPublishedAt())
                .tenantId(course.getTenantId())
                .build();
    }

    private String buildContent(Course course) {
        return safeModules(course).stream()
                .flatMap(module -> safeLessons(module).stream())
                .map(Lesson::getContent)
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(" "));
    }

    private List<Module> safeModules(Course course) {
        return course.getModules() != null ? course.getModules() : new ArrayList<>();
    }

    private List<Lesson> safeLessons(Module module) {
        return module.getLessons() != null ? module.getLessons() : new ArrayList<>();
    }

    private String resolveStatus(SearchFilter filter) {
        if (filter != null && StringUtils.hasText(filter.getStatus())) {
            return filter.getStatus().trim().toUpperCase();
        }

        return DEFAULT_STATUS;
    }

    private String resolveTenantId(String tenantId) {
        return StringUtils.hasText(tenantId) ? tenantId : "default";
    }
}
