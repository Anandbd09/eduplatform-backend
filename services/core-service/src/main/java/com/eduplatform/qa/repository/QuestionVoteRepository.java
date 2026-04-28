package com.eduplatform.qa.repository;

import com.eduplatform.qa.model.QuestionVote;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionVoteRepository extends MongoRepository<QuestionVote, String> {

    /**
     * Find existing vote for question/answer
     */
    QuestionVote findByQuestionIdAndUserIdAndVoteType(String questionId, String userId, String voteType);

    /**
     * Find vote for answer
     */
    QuestionVote findByAnswerIdAndUserId(String answerId, String userId);
}