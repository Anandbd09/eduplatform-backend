package com.eduplatform.qa.controller;

import com.eduplatform.qa.service.QuestionService;
import com.eduplatform.qa.service.AnswerService;
import com.eduplatform.qa.dto.QuestionRequest;
import com.eduplatform.qa.dto.AnswerRequest;
import com.eduplatform.qa.exception.QAException;
import com.eduplatform.core.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/qa")
public class QAController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AnswerService answerService;

    /**
     * CREATE QUESTION
     * POST /api/v1/qa/courses/{courseId}/questions
     */
    @PostMapping("/courses/{courseId}/questions")
    public ResponseEntity<?> createQuestion(
            @PathVariable String courseId,
            @RequestBody QuestionRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Name", required = false) String userName,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            var question = questionService.createQuestion(courseId, request, userId, userName, userEmail, tenantId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(question, "Question created successfully"));
        } catch (QAException e) {
            log.warn("Question creation error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "QA_ERROR"));
        } catch (Exception e) {
            log.error("Error creating question", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create question", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * GET COURSE QUESTIONS
     * GET /api/v1/qa/courses/{courseId}/questions?page=0&size=10&sort=newest
     */
    @GetMapping("/courses/{courseId}/questions")
    public ResponseEntity<?> getCourseQuestions(
            @PathVariable String courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            Page<?> questions = questionService.getCourseQuestions(courseId, page, size, sort, tenantId);
            return ResponseEntity.ok(ApiResponse.success(questions, "Questions retrieved"));
        } catch (QAException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "QA_ERROR"));
        } catch (Exception e) {
            log.error("Error fetching questions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch questions", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * GET QUESTION BY ID
     * GET /api/v1/qa/questions/{questionId}
     */
    @GetMapping("/questions/{questionId}")
    public ResponseEntity<?> getQuestion(
            @PathVariable String questionId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            var question = questionService.getQuestion(questionId, tenantId);
            return ResponseEntity.ok(ApiResponse.success(question, "Question retrieved"));
        } catch (QAException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "QA_ERROR"));
        } catch (Exception e) {
            log.error("Error fetching question", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch question", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * UPDATE QUESTION
     * PUT /api/v1/qa/questions/{questionId}
     */
    @PutMapping("/questions/{questionId}")
    public ResponseEntity<?> updateQuestion(
            @PathVariable String questionId,
            @RequestBody QuestionRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            var question = questionService.updateQuestion(questionId, request, userId, tenantId);
            return ResponseEntity.ok(ApiResponse.success(question, "Question updated"));
        } catch (QAException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "QA_ERROR"));
        } catch (Exception e) {
            log.error("Error updating question", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update question", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * DELETE QUESTION
     * DELETE /api/v1/qa/questions/{questionId}
     */
    @DeleteMapping("/questions/{questionId}")
    public ResponseEntity<?> deleteQuestion(
            @PathVariable String questionId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            questionService.deleteQuestion(questionId, userId, tenantId);
            return ResponseEntity.ok(ApiResponse.success(null, "Question deleted"));
        } catch (QAException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "QA_ERROR"));
        } catch (Exception e) {
            log.error("Error deleting question", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete question", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * CLOSE QUESTION
     * POST /api/v1/qa/questions/{questionId}/close
     */
    @PostMapping("/questions/{questionId}/close")
    public ResponseEntity<?> closeQuestion(
            @PathVariable String questionId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            var question = questionService.closeQuestion(questionId, userId, tenantId);
            return ResponseEntity.ok(ApiResponse.success(question, "Question closed"));
        } catch (QAException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "QA_ERROR"));
        } catch (Exception e) {
            log.error("Error closing question", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to close question", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * VOTE QUESTION
     * POST /api/v1/qa/questions/{questionId}/vote?upvote=true
     */
    @PostMapping("/questions/{questionId}/vote")
    public ResponseEntity<?> voteQuestion(
            @PathVariable String questionId,
            @RequestParam boolean upvote,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            questionService.voteQuestion(questionId, userId, upvote, tenantId);
            return ResponseEntity.ok(ApiResponse.success(null, upvote ? "Upvoted" : "Downvoted"));
        } catch (QAException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "QA_ERROR"));
        } catch (Exception e) {
            log.error("Error voting on question", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to vote", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * GET STATISTICS
     * GET /api/v1/qa/courses/{courseId}/statistics
     */
    @GetMapping("/courses/{courseId}/statistics")
    public ResponseEntity<?> getStatistics(
            @PathVariable String courseId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            var stats = questionService.getQuestionStatistics(courseId, tenantId);
            return ResponseEntity.ok(ApiResponse.success(stats, "Statistics retrieved"));
        } catch (QAException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "QA_ERROR"));
        } catch (Exception e) {
            log.error("Error fetching statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch statistics", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * CREATE ANSWER
     * POST /api/v1/qa/questions/{questionId}/answers
     */
    @PostMapping("/questions/{questionId}/answers")
    public ResponseEntity<?> createAnswer(
            @PathVariable String questionId,
            @RequestBody AnswerRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Name", required = false) String userName,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            var answer = answerService.createAnswer(questionId, request, userId, userName, userEmail, tenantId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(answer, "Answer created"));
        } catch (QAException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "QA_ERROR"));
        } catch (Exception e) {
            log.error("Error creating answer", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create answer", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * GET QUESTION ANSWERS
     * GET /api/v1/qa/questions/{questionId}/answers?page=0&size=10&sort=votes
     */
    @GetMapping("/questions/{questionId}/answers")
    public ResponseEntity<?> getAnswers(
            @PathVariable String questionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "votes") String sort,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            Page<?> answers = answerService.getQuestionAnswers(questionId, page, size, sort, tenantId);
            return ResponseEntity.ok(ApiResponse.success(answers, "Answers retrieved"));
        } catch (QAException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "QA_ERROR"));
        } catch (Exception e) {
            log.error("Error fetching answers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch answers", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * UPDATE ANSWER
     * PUT /api/v1/qa/answers/{answerId}
     */
    @PutMapping("/answers/{answerId}")
    public ResponseEntity<?> updateAnswer(
            @PathVariable String answerId,
            @RequestBody AnswerRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            var answer = answerService.updateAnswer(answerId, request, userId, tenantId);
            return ResponseEntity.ok(ApiResponse.success(answer, "Answer updated"));
        } catch (QAException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "QA_ERROR"));
        } catch (Exception e) {
            log.error("Error updating answer", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update answer", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * DELETE ANSWER
     * DELETE /api/v1/qa/answers/{answerId}?questionId=q123
     */
    @DeleteMapping("/answers/{answerId}")
    public ResponseEntity<?> deleteAnswer(
            @PathVariable String answerId,
            @RequestParam String questionId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            answerService.deleteAnswer(answerId, userId, questionId, tenantId);
            return ResponseEntity.ok(ApiResponse.success(null, "Answer deleted"));
        } catch (QAException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "QA_ERROR"));
        } catch (Exception e) {
            log.error("Error deleting answer", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete answer", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * MARK BEST ANSWER
     * POST /api/v1/qa/answers/{answerId}/best
     */
    @PostMapping("/answers/{answerId}/best")
    public ResponseEntity<?> markBestAnswer(
            @PathVariable String answerId,
            @RequestParam String questionId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            answerService.markBestAnswer(answerId, questionId, userId, tenantId);
            return ResponseEntity.ok(ApiResponse.success(null, "Answer marked as best"));
        } catch (QAException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "QA_ERROR"));
        } catch (Exception e) {
            log.error("Error marking best answer", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to mark best answer", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * VOTE ANSWER
     * POST /api/v1/qa/answers/{answerId}/vote?upvote=true
     */
    @PostMapping("/answers/{answerId}/vote")
    public ResponseEntity<?> voteAnswer(
            @PathVariable String answerId,
            @RequestParam boolean upvote,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            answerService.voteAnswer(answerId, userId, upvote, tenantId);
            return ResponseEntity.ok(ApiResponse.success(null, upvote ? "Upvoted" : "Downvoted"));
        } catch (QAException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "QA_ERROR"));
        } catch (Exception e) {
            log.error("Error voting on answer", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to vote", "INTERNAL_SERVER_ERROR"));
        }
    }
}
