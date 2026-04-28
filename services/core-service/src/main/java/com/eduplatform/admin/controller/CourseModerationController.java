package com.eduplatform.admin.controller;

import com.eduplatform.admin.service.CourseModerationService;
import com.eduplatform.core.common.response.ApiResponse;
import com.eduplatform.core.common.security.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/courses")
public class CourseModerationController {

    @Autowired
    private CourseModerationService courseModerationService;

    @Autowired
    private RequestContext requestContext;

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPendingCourses() {
        try {
            var courses = courseModerationService.getPendingCoursesForModeration(requestContext.getTenantId());
            return ResponseEntity.ok(ApiResponse.success(courses, "Pending courses retrieved"));
        } catch (Exception e) {
            log.error("Error fetching pending courses", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "COURSE_PENDING_FETCH_FAILED"));
        }
    }

    @GetMapping("/{courseId}/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getCourseReports(@PathVariable String courseId) {
        try {
            var reports = courseModerationService.getCourseReports(courseId, requestContext.getTenantId());
            return ResponseEntity.ok(ApiResponse.success(reports, "Course reports retrieved"));
        } catch (Exception e) {
            log.error("Error fetching reports for course {}", courseId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "COURSE_REPORTS_FETCH_FAILED"));
        }
    }

    @PostMapping("/{courseId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveCourse(@PathVariable String courseId,
                                           @RequestParam String notes) {
        try {
            courseModerationService.approveCourse(requestContext.getUserId(), courseId, notes, requestContext.getTenantId());
            return ResponseEntity.ok(ApiResponse.success(null, "Course approved"));
        } catch (Exception e) {
            log.error("Error approving course {}", courseId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "COURSE_APPROVE_FAILED"));
        }
    }

    @PostMapping("/{courseId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> rejectCourse(@PathVariable String courseId,
                                          @RequestParam String reason) {
        try {
            courseModerationService.rejectCourse(requestContext.getUserId(), courseId, reason, requestContext.getTenantId());
            return ResponseEntity.ok(ApiResponse.success(null, "Course rejected"));
        } catch (Exception e) {
            log.error("Error rejecting course {}", courseId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "COURSE_REJECT_FAILED"));
        }
    }

    @PostMapping("/{courseId}/remove")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> removeCourse(@PathVariable String courseId,
                                          @RequestParam String reason) {
        try {
            courseModerationService.removeCourse(requestContext.getUserId(), courseId, reason, requestContext.getTenantId());
            return ResponseEntity.ok(ApiResponse.success(null, "Course removed"));
        } catch (Exception e) {
            log.error("Error removing course {}", courseId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "COURSE_REMOVE_FAILED"));
        }
    }
}
