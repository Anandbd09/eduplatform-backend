package com.eduplatform.qa.service;

import com.eduplatform.qa.model.Answer;
import com.eduplatform.qa.model.Question;
import com.eduplatform.qa.dto.AnswerRequest;
import com.eduplatform.qa.dto.AnswerResponse;
import com.eduplatform.qa.model.QuestionVote;
import com.eduplatform.qa.repository.AnswerRepository;
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
public class AnswerService {

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionVoteRepository questionVoteRepository;

    /**
     * Create a new answer
     */
    public AnswerResponse createAnswer(String questionId, AnswerRequest request,
                                       String userId, String userName, String userEmail, String tenantId) {

        // Validation
        if (questionId == null || questionId.isEmpty()) {
            throw new QAException("Question ID is required");
        }
        if (request == null) {
            throw new QAException("Answer request is required");
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new QAException("Answer content is required");
        }
        if (request.getContent().length() < 20) {
            throw new QAException("Answer must be at least 20 characters");
        }
        if (request.getContent().length() > 5000) {
            throw new QAException("Answer cannot exceed 5000 characters");
        }

        // Check if question exists
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new QAException("Question not found"));

        try {
            Answer answer = Answer.builder()
                    .id(UUID.randomUUID().toString())
                    .questionId(questionId)
                    .userId(userId)
                    .userName(userName)
                    .userEmail(userEmail)
                    .content(request.getContent().trim())
                    .isBestAnswer(false)
                    .upVotes(0)
                    .downVotes(0)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .tenantId(tenantId)
                    .version(0L)
                    .build();

            Answer saved = answerRepository.save(answer);

            // Update question
            question.setAnswerCount((question.getAnswerCount() == null ? 0 : question.getAnswerCount()) + 1);
            question.setStatus("ANSWERED");
            questionRepository.save(question);

            log.info("Answer created: {} for question: {}", saved.getId(), questionId);
            return convertToResponse(saved);

        } catch (QAException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating answer", e);
            throw new QAException("Failed to create answer: " + e.getMessage());
        }
    }

    /**
     * Get all answers for a question
     */
    public Page<AnswerResponse> getQuestionAnswers(String questionId, int page, int size, String sortBy, String tenantId) {

        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);

            Pageable pageable = createPageable(page, size, sortBy);
            Page<Answer> answers = answerRepository.findByQuestionIdAndTenantId(questionId, tenantId, pageable);

            return answers.map(this::convertToResponse);

        } catch (Exception e) {
            log.error("Error fetching answers", e);
            throw new QAException("Failed to fetch answers");
        }
    }

    /**
     * Get answer by ID
     */
    public AnswerResponse getAnswer(String answerId, String tenantId) {

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new QAException("Answer not found"));

        return convertToResponse(answer);
    }

    /**
     * Update an answer
     */
    public AnswerResponse updateAnswer(String answerId, AnswerRequest request,
                                       String userId, String tenantId) {

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new QAException("Answer not found"));

        // Check ownership
        if (!answer.getUserId().equals(userId)) {
            throw new QAException("You can only edit your own answers");
        }

        // Cannot edit if marked as best
        if (answer.getIsBestAnswer()) {
            throw new QAException("Cannot edit answers marked as best");
        }

        try {
            if (request.getContent() != null && !request.getContent().isEmpty()) {
                answer.setContent(request.getContent().trim());
            }

            answer.setUpdatedAt(LocalDateTime.now());
            answer.setVersion(answer.getVersion() + 1);

            Answer updated = answerRepository.save(answer);
            log.info("Answer updated: {}", answerId);
            return convertToResponse(updated);

        } catch (QAException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating answer", e);
            throw new QAException("Failed to update answer");
        }
    }

    /**
     * Delete an answer
     */
    public void deleteAnswer(String answerId, String userId, String questionId, String tenantId) {

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new QAException("Answer not found"));

        // Check ownership
        if (!answer.getUserId().equals(userId)) {
            throw new QAException("You can only delete your own answers");
        }

        // Cannot delete best answer
        if (answer.getIsBestAnswer()) {
            throw new QAException("Cannot delete answers marked as best");
        }

        try {
            answerRepository.deleteById(answerId);

            // Update question
            Question question = questionRepository.findById(questionId)
                    .orElseThrow(() -> new QAException("Question not found"));
            question.setAnswerCount(Math.max(0, question.getAnswerCount() - 1));
            questionRepository.save(question);

            log.info("Answer deleted: {}", answerId);

        } catch (QAException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting answer", e);
            throw new QAException("Failed to delete answer");
        }
    }

    /**
     * Mark answer as best
     */
    public void markBestAnswer(String answerId, String questionId, String userId, String tenantId) {

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new QAException("Question not found"));

        // Check ownership of question
        if (!question.getUserId().equals(userId)) {
            throw new QAException("Only question author can mark best answer");
        }

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new QAException("Answer not found"));

        try {
            // Remove previous best answer
            if (question.getBestAnswerId() != null) {
                Answer previousBest = answerRepository.findById(question.getBestAnswerId())
                        .orElse(null);
                if (previousBest != null) {
                    previousBest.setIsBestAnswer(false);
                    answerRepository.save(previousBest);
                }
            }

            // Mark new best answer
            answer.setIsBestAnswer(true);
            answerRepository.save(answer);

            question.setBestAnswerId(answerId);
            question.setStatus("CLOSED");
            questionRepository.save(question);

            log.info("Answer marked as best: {}", answerId);

        } catch (Exception e) {
            log.error("Error marking best answer", e);
            throw new QAException("Failed to mark best answer");
        }
    }

    /**
     * Vote on an answer
     */
    public void voteAnswer(String answerId, String userId, boolean isUpVote, String tenantId) {

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new QAException("Answer not found"));

        try {
            // Prevent self-voting
            if (answer.getUserId().equals(userId)) {
                throw new QAException("Cannot vote on your own answer");
            }

            // Check existing vote
            var existingVote = questionVoteRepository.findByAnswerIdAndUserId(answerId, userId);

            if (existingVote != null) {
                if (existingVote.getIsUpVote() == isUpVote) {
                    // Same vote - remove it
                    questionVoteRepository.delete(existingVote);

                    if (isUpVote) {
                        answer.setUpVotes(Math.max(0, answer.getUpVotes() - 1));
                    } else {
                        answer.setDownVotes(Math.max(0, answer.getDownVotes() - 1));
                    }
                } else {
                    // Different vote - switch it
                    existingVote.setIsUpVote(isUpVote);
                    questionVoteRepository.save(existingVote);

                    if (isUpVote) {
                        answer.setUpVotes(answer.getUpVotes() + 1);
                        answer.setDownVotes(Math.max(0, answer.getDownVotes() - 1));
                    } else {
                        answer.setDownVotes(answer.getDownVotes() + 1);
                        answer.setUpVotes(Math.max(0, answer.getUpVotes() - 1));
                    }
                }
            } else {
                // New vote
                var newVote = QuestionVote.builder()
                        .id(UUID.randomUUID().toString())
                        .answerId(answerId)
                        .userId(userId)
                        .isUpVote(isUpVote)
                        .votedAt(LocalDateTime.now())
                        .voteType("ANSWER")
                        .build();

                questionVoteRepository.save(newVote);

                if (isUpVote) {
                    answer.setUpVotes(answer.getUpVotes() + 1);
                } else {
                    answer.setDownVotes(answer.getDownVotes() + 1);
                }
            }

            answerRepository.save(answer);

        } catch (QAException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error voting on answer", e);
            throw new QAException("Failed to vote on answer");
        }
    }

    /**
     * Create pageable
     */
    private Pageable createPageable(int page, int size, String sortBy) {
        return switch (sortBy != null ? sortBy : "newest") {
            case "newest" -> PageRequest.of(page, size, Sort.by("createdAt").descending());
            case "oldest" -> PageRequest.of(page, size, Sort.by("createdAt").ascending());
            case "votes" -> PageRequest.of(page, size, Sort.by("upVotes").descending());
            case "best" -> PageRequest.of(page, size, Sort.by("isBestAnswer").descending());
            default -> PageRequest.of(page, size, Sort.by("createdAt").descending());
        };
    }

    /**
     * Convert to response
     */
    private AnswerResponse convertToResponse(Answer answer) {
        if (answer == null) return null;

        return AnswerResponse.builder()
                .id(answer.getId())
                .questionId(answer.getQuestionId())
                .userId(answer.getUserId())
                .userName(answer.getUserName())
                .userEmail(answer.getUserEmail())
                .userAvatar(answer.getUserAvatar())
                .content(answer.getContent())
                .isBestAnswer(answer.getIsBestAnswer())
                .upVotes(answer.getUpVotes())
                .downVotes(answer.getDownVotes())
                .netVotes(answer.getNetVotes())
                .createdAt(answer.getCreatedAt())
                .updatedAt(answer.getUpdatedAt())
                .build();
    }
}