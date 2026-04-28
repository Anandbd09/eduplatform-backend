package com.eduplatform.recommendation.service;

import com.eduplatform.recommendation.model.*;
import com.eduplatform.recommendation.repository.*;
import com.eduplatform.recommendation.util.SimilarityCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@Transactional
public class SimilarityService {

    @Autowired
    private CourseVectorRepository courseVectorRepository;

    @Autowired
    private CourseSimilarityRepository courseSimilarityRepository;

    /**
     * Calculate all course similarities (batch operation)
     */
    public void calculateCourseSimilarities(String tenantId) {
        try {
            List<CourseVector> courses = courseVectorRepository.findAll();
            List<CourseSimilarity> similarities = new ArrayList<>();

            for (int i = 0; i < courses.size(); i++) {
                for (int j = i + 1; j < courses.size(); j++) {
                    CourseVector course1 = courses.get(i);
                    CourseVector course2 = courses.get(j);

                    Double similarity = calculateSimilarity(course1, course2);

                    if (similarity >= 0.5) { // Only save if similarity > 0.5
                        CourseSimilarity record = CourseSimilarity.builder()
                                .id(UUID.randomUUID().toString())
                                .sourceCourseId(course1.getCourseId())
                                .targetCourseId(course2.getCourseId())
                                .similarityScore(similarity)
                                .calculatedAt(LocalDateTime.now())
                                .tenantId(tenantId)
                                .build();
                        similarities.add(record);
                    }
                }
            }

            courseSimilarityRepository.saveAll(similarities);
            log.info("Calculated {} course similarities", similarities.size());

        } catch (Exception e) {
            log.error("Error calculating course similarities", e);
        }
    }

    /**
     * Calculate similarity between two courses
     */
    public Double calculateSimilarity(CourseVector course1, CourseVector course2) {
        if (course1.getFeatureVector() == null || course2.getFeatureVector() == null) {
            return 0.0;
        }
        return SimilarityCalculator.featureSimilarity(course1.getFeatureVector(), course2.getFeatureVector());
    }

    /**
     * Get similar courses
     */
    public List<CourseSimilarity> getSimilarCourses(String courseId, String tenantId, int limit) {
        try {
            List<CourseSimilarity> similarities = courseSimilarityRepository
                    .findBySourceCourseIdAndSimilarityScoreGreaterThanAndTenantId(courseId, 0.7, tenantId);

            return similarities.stream()
                    .limit(limit)
                    .toList();

        } catch (Exception e) {
            log.error("Error getting similar courses", e);
            return new ArrayList<>();
        }
    }
}