package com.eduplatform.qa.service;

import com.eduplatform.qa.model.Question;
import com.eduplatform.qa.dto.QuestionRequest;
import com.eduplatform.qa.dto.QuestionResponse;
import com.eduplatform.qa.dto.QuestionStatistics;
import com.eduplatform.qa.model.QuestionVote;
import com.eduplatform.qa.repository.QuestionRepository;
import com.eduplatform.qa.repository.QuestionVoteRepository;
import com.eduplatform.qa.exception.QAException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionVoteRepository questionVoteRepository;

    /**
     * Create a new question
     */
    public QuestionResponse createQuestion(String courseId, QuestionRequest request,
                                           String userId, String userName, String userEmail, String tenantId) {

        // Validation
        if (courseId == null || courseId.isEmpty()) {
            throw new QAException("Course ID is required");
        }
        if (request == null) {
            throw new QAException("Question request is required");
        }
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new QAException("Question title is required");
        }
        if (request.getTitle().length() < 5) {
            throw new QAException("Title must be at least 5 characters");
        }
        if (request.getTitle().length() > 200) {
            throw new QAException("Title cannot exceed 200 characters");
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new QAException("Question content is required");
        }
        if (request.getContent().length() < 20) {
            throw new QAException("Content must be at least 20 characters");
        }
        if (request.getContent().length() > 5000) {
            throw new QAException("Content cannot exceed 5000 characters");
        }

        try {
            Question question = Question.builder()
                    .id(UUID.randomUUID().toString())
                    .courseId(courseId)
                    .userId(userId)
                    .userName(userName)
                    .userEmail(userEmail)
                    .title(request.getTitle().trim())
                    .content(request.getContent().trim())
                    .status("OPEN")
                    .views(0)
                    .upVotes(0)
                    .downVotes(0)
                    .answerCount(0)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .tenantId(tenantId)
                    .tags(request.getTags())
                    .version(0L)
                    .build();

            Question saved = questionRepository.save(question);
            log.info("Question created: {} by user: {}", saved.getId(), userId);
            return convertToResponse(saved);

        } catch (QAException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating question", e);
            throw new QAException("Failed to create question: " + e.getMessage());
        }
    }

    /**
     * Get all questions for a course
     */
    public Page<QuestionResponse> getCourseQuestions(String courseId, int page, int size, String sortBy, String tenantId) {

        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);

            Pageable pageable = createPageable(page, size, sortBy);
            Page<Question> questions = questionRepository.findByCourseIdAndTenantId(courseId, tenantId, pageable);

            return questions.map(this::convertToResponse);

        } catch (Exception e) {
            log.error("Error fetching questions", e);
            throw new QAException("Failed to fetch questions");
        }
    }

    /**
     * Get question by ID
     */
    public QuestionResponse getQuestion(String questionId, String tenantId) {

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new QAException("Question not found"));

        // Increment views
        question.incrementViews();
        questionRepository.save(question);

        return convertToResponse(question);
    }

    /**
     * Update a question
     */
    public QuestionResponse updateQuestion(String questionId, QuestionRequest request,
                                           String userId, String tenantId) {

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new QAException("Question not found"));

        // Check ownership
        if (!question.getUserId().equals(userId)) {
            throw new QAException("You can only edit your own questions");
        }

        // Check if answered
        if (question.isAnswered()) {
            throw new QAException("Cannot edit questions that have answers");
        }

        try {
            if (request.getTitle() != null && !request.getTitle().isEmpty()) {
                question.setTitle(request.getTitle().trim());
            }
            if (request.getContent() != null && !request.getContent().isEmpty()) {
                question.setContent(request.getContent().trim());
            }

            question.setUpdatedAt(LocalDateTime.now());
            question.setVersion(question.getVersion() + 1);

            Question updated = questionRepository.save(question);
            log.info("Question updated: {}", questionId);
            return convertToResponse(updated);

        } catch (QAException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating question", e);
            throw new QAException("Failed to update question");
        }
    }

    /**
     * Delete a question
     */
    public void deleteQuestion(String questionId, String userId, String tenantId) {

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new QAException("Question not found"));

        // Check ownership
        if (!question.getUserId().equals(userId)) {
            throw new QAException("You can only delete your own questions");
        }

        try {
            questionRepository.deleteById(questionId);
            log.info("Question deleted: {}", questionId);
        } catch (Exception e) {
            log.error("Error deleting question", e);
            throw new QAException("Failed to delete question");
        }
    }

    /**
     * Close a question
     */
    public QuestionResponse closeQuestion(String questionId, String userId, String tenantId) {

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new QAException("Question not found"));

        // Check ownership
        if (!question.getUserId().equals(userId)) {
            throw new QAException("Only question author can close it");
        }

        try {
            question.setStatus("CLOSED");
            question.setUpdatedAt(LocalDateTime.now());
            Question updated = questionRepository.save(question);

            log.info("Question closed: {}", questionId);
            return convertToResponse(updated);

        } catch (Exception e) {
            log.error("Error closing question", e);
            throw new QAException("Failed to close question");
        }
    }

    /**
     * Vote on a question
     */
    public void voteQuestion(String questionId, String userId, boolean isUpVote, String tenantId) {

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new QAException("Question not found"));

        try {
            // Prevent self-voting
            if (question.getUserId().equals(userId)) {
                throw new QAException("Cannot vote on your own question");
            }

            // Check existing vote
            var existingVote = questionVoteRepository.findByQuestionIdAndUserIdAndVoteType(questionId, userId, "QUESTION");

            if (existingVote != null) {
                // User already voted
                if (existingVote.getIsUpVote() == isUpVote) {
                    // Same vote - remove it
                    questionVoteRepository.delete(existingVote);

                    if (isUpVote) {
                        question.setUpVotes(Math.max(0, question.getUpVotes() - 1));
                    } else {
                        question.setDownVotes(Math.max(0, question.getDownVotes() - 1));
                    }
                } else {
                    // Different vote - switch it
                    existingVote.setIsUpVote(isUpVote);
                    questionVoteRepository.save(existingVote);

                    if (isUpVote) {
                        question.setUpVotes(question.getUpVotes() + 1);
                        question.setDownVotes(Math.max(0, question.getDownVotes() - 1));
                    } else {
                        question.setDownVotes(question.getDownVotes() + 1);
                        question.setUpVotes(Math.max(0, question.getUpVotes() - 1));
                    }
                }
            } else {
                // New vote
                var newVote = QuestionVote.builder()
                        .id(UUID.randomUUID().toString())
                        .questionId(questionId)
                        .userId(userId)
                        .isUpVote(isUpVote)
                        .votedAt(LocalDateTime.now())
                        .voteType("QUESTION")
                        .build();

                questionVoteRepository.save(newVote);

                if (isUpVote) {
                    question.setUpVotes(question.getUpVotes() + 1);
                } else {
                    question.setDownVotes(question.getDownVotes() + 1);
                }
            }

            questionRepository.save(question);

        } catch (QAException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error voting on question", e);
            throw new QAException("Failed to vote on question");
        }
    }

    /**
     * Get question statistics
     */
    public QuestionStatistics getQuestionStatistics(String courseId, String tenantId) {

        try {
            Long totalQuestions = questionRepository.countByCourseIdAndTenantId(courseId, tenantId);
            Long openQuestions = questionRepository.countByStatusAndCourseIdAndTenantId("OPEN", courseId, tenantId);
            Long answeredQuestions = questionRepository.countByStatusAndCourseIdAndTenantId("ANSWERED", courseId, tenantId);
            Long closedQuestions = questionRepository.countByStatusAndCourseIdAndTenantId("CLOSED", courseId, tenantId);

            return QuestionStatistics.builder()
                    .totalQuestions(totalQuestions)
                    .openQuestions(openQuestions)
                    .answeredQuestions(answeredQuestions)
                    .closedQuestions(closedQuestions)
                    .build();

        } catch (Exception e) {
            log.error("Error getting statistics", e);
            throw new QAException("Failed to get statistics");
        }
    }

    /**
     * Create pageable with sorting
     */
    private Pageable createPageable(int page, int size, String sortBy) {
        return switch (sortBy != null ? sortBy : "newest") {
            case "newest" -> PageRequest.of(page, size, Sort.by("createdAt").descending());
            case "oldest" -> PageRequest.of(page, size, Sort.by("createdAt").ascending());
            case "popular" -> PageRequest.of(page, size, Sort.by("views").descending());
            case "votes" -> PageRequest.of(page, size, Sort.by("upVotes").descending());
            case "unanswered" -> PageRequest.of(page, size, Sort.by("answerCount").ascending());
            default -> PageRequest.of(page, size, Sort.by("createdAt").descending());
        };
    }

    /**
     * Convert to response
     */
    private QuestionResponse convertToResponse(Question question) {
        if (question == null) return null;

        return QuestionResponse.builder()
                .id(question.getId())
                .courseId(question.getCourseId())
                .userId(question.getUserId())
                .userName(question.getUserName())
                .userEmail(question.getUserEmail())
                .userAvatar(question.getUserAvatar())
                .title(question.getTitle())
                .content(question.getContent())
                .status(question.getStatus())
                .views(question.getViews())
                .upVotes(question.getUpVotes())
                .downVotes(question.getDownVotes())
                .netVotes(question.getNetVotes())
                .answerCount(question.getAnswerCount())
                .createdAt(question.getCreatedAt())
                .updatedAt(question.getUpdatedAt())
                .isAnswered(question.isAnswered())
                .hasBestAnswer(question.hasBestAnswer())
                .bestAnswerId(question.getBestAnswerId())
                .tags(question.getTags())
                .build();
    }
}