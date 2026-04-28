package com.eduplatform.qa.repository;

import com.eduplatform.qa.model.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends MongoRepository<Question, String> {

    /**
     * Find all questions for a course with pagination
     */
    Page<Question> findByCourseIdAndTenantId(String courseId, String tenantId, Pageable pageable);

    /**
     * Find all questions by status
     */
    Page<Question> findByStatusAndCourseIdAndTenantId(String status, String courseId, String tenantId, Pageable pageable);

    /**
     * Find all questions by user
     */
    List<Question> findByUserIdAndTenantId(String userId, String tenantId);

    /**
     * Find unanswered questions
     */
    Page<Question> findByStatusAndCourseIdAndAnswerCountAndTenantId(
            String status, String courseId, Integer answerCount, String tenantId, Pageable pageable
    );

    /**
     * Find questions with best answer
     */
    @Query("{ 'courseId': ?0, 'bestAnswerId': { $ne: null }, 'tenantId': ?1 }")
    Page<Question> findQuestionsWithBestAnswer(String courseId, String tenantId, Pageable pageable);

    /**
     * Count questions by status
     */
    Long countByStatusAndCourseIdAndTenantId(String status, String courseId, String tenantId);

    /**
     * Find questions by tag
     */
    @Query("{ 'courseId': ?0, 'tags': ?1, 'tenantId': ?2 }")
    Page<Question> findByTag(String courseId, String tag, String tenantId, Pageable pageable);

    Long countByCourseIdAndTenantId(String courseId, String tenantId);
}