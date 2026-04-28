package com.eduplatform.qa.repository;

import com.eduplatform.qa.model.Answer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnswerRepository extends MongoRepository<Answer, String> {

    /**
     * Find all answers for a question
     */
    Page<Answer> findByQuestionIdAndTenantId(String questionId, String tenantId, Pageable pageable);

    /**
     * Find all answers by user
     */
    List<Answer> findByUserIdAndTenantId(String userId, String tenantId);

    /**
     * Find best answer for a question
     */
    Answer findByQuestionIdAndIsBestAnswerTrueAndTenantId(String questionId, String tenantId);

    /**
     * Count answers for a question
     */
    Long countByQuestionIdAndTenantId(String questionId, String tenantId);

    /**
     * Find answers sorted by votes
     */
    @Query("{ 'questionId': ?0, 'tenantId': ?1 }")
    Page<Answer> findAnswersByVotes(String questionId, String tenantId, Pageable pageable);

    /**
     * Find most helpful answers
     */
    @Query("{ 'questionId': ?0, 'upVotes': { $gte: ?1 }, 'tenantId': ?2 }")
    List<Answer> findMostHelpfulAnswers(String questionId, Integer minVotes, String tenantId);
}