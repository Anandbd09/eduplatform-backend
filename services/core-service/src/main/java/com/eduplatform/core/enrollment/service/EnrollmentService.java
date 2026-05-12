package com.eduplatform.core.enrollment.service;

import com.eduplatform.core.course.model.Course;
import com.eduplatform.core.course.repository.CourseRepository;
import com.eduplatform.core.enrollment.dto.EnrollmentResponse;
import com.eduplatform.core.enrollment.dto.EnrolledStudentResponse;
import com.eduplatform.core.enrollment.dto.ProgressResponse;
import com.eduplatform.core.enrollment.model.Enrollment;
import com.eduplatform.core.enrollment.repository.EnrollmentRepository;
import com.eduplatform.core.common.exception.AppException;
import com.eduplatform.core.user.model.User;
import com.eduplatform.core.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EnrollmentService {

        @Autowired
        private EnrollmentRepository enrollmentRepository;

        @Autowired
        private CourseRepository courseRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private MongoTemplate mongoTemplate;

        public EnrollmentResponse enrollStudent(String courseId, String userId, String tenantId) {

                Course course = courseRepository.findByIdAndTenantId(courseId, tenantId)
                                .orElseThrow(() -> AppException.notFound("Course not found"));

                if (!"PUBLISHED".equals(course.getStatus())) {
                        throw AppException.badRequest("Only published courses can be enrolled");
                }

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
                incrementCourseEnrollmentCount(courseId, tenantId);

                log.info("Student {} enrolled in course {}", userId, courseId);

                return mapToEnrollmentResponse(enrollment, course);
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

                List<Enrollment> enrollments = enrollmentRepository.findByUserIdAndTenantId(userId, tenantId);
                Map<String, Course> coursesById = courseRepository.findAllById(
                                enrollments.stream()
                                                .map(Enrollment::getCourseId)
                                                .collect(Collectors.toSet())
                                )
                                .stream()
                                .filter(course -> tenantId.equals(course.getTenantId()))
                                .collect(Collectors.toMap(Course::getId, Function.identity()));

                return enrollments.stream()
                                .map(enrollment -> mapToEnrollmentResponse(enrollment, coursesById.get(enrollment.getCourseId())))
                                .collect(Collectors.toList());
        }

        public List<EnrolledStudentResponse> getEnrolledStudentsForInstructor(
                        String courseId,
                        String instructorId,
                        String tenantId) {

                Course course = courseRepository.findByIdAndTenantId(courseId, tenantId)
                                .orElseThrow(() -> AppException.notFound("Course not found"));

                if (!course.getInstructorId().equals(instructorId)) {
                        throw AppException.unauthorized("You don't have permission to view this course");
                }

                List<Enrollment> enrollments = enrollmentRepository
                                .findByCourseIdAndTenantIdOrderByEnrolledAtDesc(courseId, tenantId);
                Map<String, User> usersById = userRepository.findAllById(
                                enrollments.stream()
                                                .map(Enrollment::getUserId)
                                                .collect(Collectors.toSet())
                                )
                                .stream()
                                .filter(user -> tenantId.equals(user.getTenantId()))
                                .collect(Collectors.toMap(User::getId, Function.identity()));

                return enrollments.stream()
                                .map(enrollment -> mapToEnrolledStudentResponse(enrollment, usersById.get(enrollment.getUserId())))
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
                return mapToEnrollmentResponse(enrollment, null);
        }

        private EnrollmentResponse mapToEnrollmentResponse(Enrollment enrollment, Course course) {
                return EnrollmentResponse.builder()
                                .id(enrollment.getId())
                                .userId(enrollment.getUserId())
                                .courseId(enrollment.getCourseId())
                                .courseTitle(course != null ? course.getTitle() : null)
                                .courseThumbnailUrl(course != null ? course.getThumbnailUrl() : null)
                                .courseStatus(course != null ? course.getStatus() : null)
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

        private EnrolledStudentResponse mapToEnrolledStudentResponse(Enrollment enrollment, User user) {
                return EnrolledStudentResponse.builder()
                                .enrollmentId(enrollment.getId())
                                .studentId(enrollment.getUserId())
                                .firstName(user != null ? user.getFirstName() : null)
                                .lastName(user != null ? user.getLastName() : null)
                                .email(user != null ? user.getEmail() : null)
                                .status(enrollment.getStatus())
                                .progressPercentage(enrollment.getProgressPercentage())
                                .completedLessonsCount(enrollment.getCompletedLessons().size())
                                .enrolledAt(enrollment.getEnrolledAt())
                                .completedAt(enrollment.getCompletedAt())
                                .build();
        }

        private void incrementCourseEnrollmentCount(String courseId, String tenantId) {
                Query query = new Query(Criteria.where("_id").is(courseId).and("tenantId").is(tenantId));
                Update update = new Update().inc("enrolledCount", 1).set("updatedAt", LocalDateTime.now());
                mongoTemplate.updateFirst(query, update, Course.class);
        }
}
