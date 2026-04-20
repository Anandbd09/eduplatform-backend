package com.eduplatform.core.enrollment.service;

import com.eduplatform.core.enrollment.dto.EnrollmentResponse;
import com.eduplatform.core.enrollment.dto.ProgressResponse;
import com.eduplatform.core.enrollment.model.Enrollment;
import com.eduplatform.core.enrollment.repository.EnrollmentRepository;
import com.eduplatform.core.common.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EnrollmentService {

        @Autowired
        private EnrollmentRepository enrollmentRepository;

        public EnrollmentResponse enrollStudent(String courseId, String userId, String tenantId) {

                // Check if already enrolled
                enrollmentRepository.findByUserIdAndCourseIdAndTenantId(userId, courseId, tenantId)
                                .ifPresent(e -> {
                                        throw AppException.conflict("Student already enrolled in this course");
                                });

                Enrollment enrollment = Enrollment.builder()
                                .tenantId(tenantId)
                                .userId(userId)
                                .courseId(courseId)
                                .status("ACTIVE")
                                .progressPercentage(0.0)
                                .enrolledAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();

                enrollment = enrollmentRepository.save(enrollment);

                log.info("Student {} enrolled in course {}", userId, courseId);

                return mapToEnrollmentResponse(enrollment);
        }

        public ProgressResponse markLessonComplete(String enrollmentId, String lessonId, String moduleId,
                        int watchTimeSeconds, String tenantId) {

                Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                                .orElseThrow(() -> AppException.notFound("Enrollment not found"));

                if (!enrollment.getTenantId().equals(tenantId)) {
                        throw AppException.unauthorized("Access denied");
                }

                Enrollment.CompletedLesson completedLesson = Enrollment.CompletedLesson.builder()
                                .lessonId(lessonId)
                                .moduleId(moduleId)
                                .completedAt(LocalDateTime.now())
                                .watchTimeSeconds(watchTimeSeconds)
                                .build();

                // Check if lesson already completed
                Enrollment.CompletedLesson existingLesson = enrollment.getCompletedLessons().stream()
                                .filter(cl -> cl.getLessonId().equals(lessonId))
                                .findFirst()
                                .orElse(null);

                if (existingLesson != null) {
                        existingLesson.setCompletedAt(LocalDateTime.now());
                } else {
                        enrollment.getCompletedLessons().add(completedLesson);
                }

                enrollment.setUpdatedAt(LocalDateTime.now());
                enrollment = enrollmentRepository.save(enrollment);

                log.info("Lesson {} marked complete for enrollment {}", lessonId, enrollmentId);

                return mapToProgressResponse(enrollment);
        }

        public EnrollmentResponse getEnrollment(String enrollmentId, String tenantId) {

                Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                                .orElseThrow(() -> AppException.notFound("Enrollment not found"));

                if (!enrollment.getTenantId().equals(tenantId)) {
                        throw AppException.unauthorized("Access denied");
                }

                return mapToEnrollmentResponse(enrollment);
        }

        public List<EnrollmentResponse> getStudentEnrollments(String userId, String tenantId) {

                return enrollmentRepository.findByUserIdAndTenantId(userId, tenantId)
                                .stream()
                                .map(this::mapToEnrollmentResponse)
                                .collect(Collectors.toList());
        }

        public EnrollmentResponse completeCourse(String enrollmentId, String tenantId) {

                Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                                .orElseThrow(() -> AppException.notFound("Enrollment not found"));

                if (!enrollment.getTenantId().equals(tenantId)) {
                        throw AppException.unauthorized("Access denied");
                }

                enrollment.setStatus("COMPLETED");
                enrollment.setCompletedAt(LocalDateTime.now());
                enrollment.setProgressPercentage(100.0);
                enrollment.setUpdatedAt(LocalDateTime.now());

                enrollment = enrollmentRepository.save(enrollment);

                log.info("Course completed for enrollment {}", enrollmentId);

                return mapToEnrollmentResponse(enrollment);
        }

        private EnrollmentResponse mapToEnrollmentResponse(Enrollment enrollment) {
                return EnrollmentResponse.builder()
                                .id(enrollment.getId())
                                .userId(enrollment.getUserId())
                                .courseId(enrollment.getCourseId())
                                .status(enrollment.getStatus())
                                .progressPercentage(enrollment.getProgressPercentage())
                                .completedLessonsCount(enrollment.getCompletedLessons().size())
                                .enrolledAt(enrollment.getEnrolledAt())
                                .completedAt(enrollment.getCompletedAt())
                                .build();
        }

        private ProgressResponse mapToProgressResponse(Enrollment enrollment) {
                return ProgressResponse.builder()
                                .enrollmentId(enrollment.getId())
                                .progressPercentage(enrollment.getProgressPercentage())
                                .completedLessonsCount(enrollment.getCompletedLessons().size())
                                .totalWatchTimeSeconds(enrollment.getCompletedLessons().stream()
                                                .mapToInt(Enrollment.CompletedLesson::getWatchTimeSeconds)
                                                .sum())
                                .updatedAt(enrollment.getUpdatedAt())
                                .build();
        }
}
