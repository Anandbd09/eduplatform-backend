package com.eduplatform.core.enrollment.controller;

import com.eduplatform.core.enrollment.dto.EnrollmentResponse;
import com.eduplatform.core.enrollment.dto.EnrollRequest;
import com.eduplatform.core.enrollment.dto.ProgressResponse;
import com.eduplatform.core.enrollment.service.EnrollmentService;
import com.eduplatform.core.common.response.ApiResponse;
import com.eduplatform.core.common.security.RequestContext;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/enrollments")
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private RequestContext requestContext;

    @PostMapping
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enrollCourse(
            @Valid @RequestBody EnrollRequest request) {

        log.info("Enroll course request for courseId: {}", request.getCourseId());

        EnrollmentResponse response = enrollmentService.enrollStudent(
                request.getCourseId(),
                requestContext.getUserId(),
                requestContext.getTenantId()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Enrolled successfully"));
    }

    @PostMapping("/{enrollmentId}/lessons/{lessonId}/complete")
    public ResponseEntity<ApiResponse<ProgressResponse>> markLessonComplete(
            @PathVariable String enrollmentId,
            @PathVariable String lessonId,
            @RequestParam String moduleId,
            @RequestParam int watchTimeSeconds) {

        log.info("Mark lesson complete: {}", lessonId);

        ProgressResponse response = enrollmentService.markLessonComplete(
                enrollmentId,
                lessonId,
                moduleId,
                watchTimeSeconds,
                requestContext.getTenantId()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response, "Progress updated"));
    }

    @GetMapping("/{enrollmentId}")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> getEnrollment(
            @PathVariable String enrollmentId) {

        EnrollmentResponse response = enrollmentService.getEnrollment(
                enrollmentId,
                requestContext.getTenantId()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response, "Enrollment retrieved"));
    }

    @GetMapping("/student/my-enrollments")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getMyEnrollments() {

        List<EnrollmentResponse> response = enrollmentService.getStudentEnrollments(
                requestContext.getUserId(),
                requestContext.getTenantId()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response, "Enrollments retrieved"));
    }

    @PostMapping("/{enrollmentId}/complete")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> completeCourse(
            @PathVariable String enrollmentId) {

        EnrollmentResponse response = enrollmentService.completeCourse(
                enrollmentId,
                requestContext.getTenantId()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response, "Course marked as completed"));
    }
}