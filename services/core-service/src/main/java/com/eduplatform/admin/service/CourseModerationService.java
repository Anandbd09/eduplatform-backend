package com.eduplatform.admin.service;

import com.eduplatform.admin.model.AuditAction;
import com.eduplatform.admin.model.CourseReport;
import com.eduplatform.admin.repository.CourseReportRepository;
import com.eduplatform.core.common.exception.AppException;
import com.eduplatform.core.course.model.Course;
import com.eduplatform.core.course.repository.CourseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class CourseModerationService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseReportRepository courseReportRepository;

    @Autowired
    private AuditLogService auditLogService;

    public void approveCourse(String adminId, String courseId, String notes, String tenantId) {
        try {
            Course course = getCourseForModeration(courseId, tenantId);

            LocalDateTime now = LocalDateTime.now();
            course.setStatus("PUBLISHED");
            course.setPublishedAt(now);
            course.setApprovedAt(now);
            course.setApprovedBy(adminId);
            course.setApprovalNotes(notes);
            course.setRejectedAt(null);
            course.setRejectedBy(null);
            course.setRejectionReason(null);
            course.setRemovedAt(null);
            course.setRemovedBy(null);
            course.setRemovalReason(null);
            course.setUpdatedAt(now);

            courseRepository.save(course);

            logAuditAction(adminId, AuditAction.COURSE_APPROVED, courseId, "Course approved: " + notes);

            log.info("Course approved: {}", courseId);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error approving course", e);
            throw AppException.internalError("Failed to approve course");
        }
    }

    public void rejectCourse(String adminId, String courseId, String reason, String tenantId) {
        try {
            Course course = getCourseForModeration(courseId, tenantId);

            LocalDateTime now = LocalDateTime.now();
            course.setStatus("REJECTED");
            course.setRejectedAt(now);
            course.setRejectedBy(adminId);
            course.setRejectionReason(reason);
            course.setApprovalNotes(null);
            course.setApprovedAt(null);
            course.setApprovedBy(null);
            course.setUpdatedAt(now);

            courseRepository.save(course);

            logAuditAction(adminId, AuditAction.COURSE_REJECTED, courseId, "Course rejected: " + reason);

            log.info("Course rejected: {}", courseId);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error rejecting course", e);
            throw AppException.internalError("Failed to reject course");
        }
    }

    public void removeCourse(String adminId, String courseId, String reason, String tenantId) {
        try {
            Course course = getCourseForModeration(courseId, tenantId);

            LocalDateTime now = LocalDateTime.now();
            course.setStatus("REMOVED");
            course.setRemovedAt(now);
            course.setRemovedBy(adminId);
            course.setRemovalReason(reason);
            course.setUpdatedAt(now);

            courseRepository.save(course);

            logAuditAction(adminId, AuditAction.COURSE_REMOVED, courseId, "Course removed: " + reason);

            log.info("Course removed: {}", courseId);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error removing course", e);
            throw AppException.internalError("Failed to remove course");
        }
    }

    public List<Course> getPendingCoursesForModeration(String tenantId) {
        return courseRepository.findByStatusAndTenantId("PENDING_APPROVAL", tenantId);
    }

    public List<CourseReport> getCourseReports(String courseId, String tenantId) {
        getCourseForModeration(courseId, tenantId);

        return courseReportRepository.findByCourseId(courseId).stream()
                .filter(report -> tenantId.equals(report.getTenantId()))
                .toList();
    }

    private Course getCourseForModeration(String courseId, String tenantId) {
        return courseRepository.findByIdAndTenantId(courseId, tenantId)
                .orElseThrow(() -> AppException.notFound("Course not found"));
    }

    private void logAuditAction(String adminId, AuditAction action, String courseId, String description) {
        try {
            auditLogService.logAction(adminId, action, courseId, "COURSE", description);
        } catch (Exception e) {
            log.error("Audit logging failed for course moderation action {} on course {}", action, courseId, e);
        }
    }
}
