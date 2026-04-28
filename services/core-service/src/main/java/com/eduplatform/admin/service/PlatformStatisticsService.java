package com.eduplatform.admin.service;

import com.eduplatform.admin.dto.PlatformStatsResponse;
import com.eduplatform.admin.model.DisputeStatus;
import com.eduplatform.admin.repository.PaymentDisputeRepository;
import com.eduplatform.admin.repository.UserReportRepository;
import com.eduplatform.core.course.repository.CourseRepository;
import com.eduplatform.core.enrollment.repository.EnrollmentRepository;
import com.eduplatform.core.user.repository.UserRepository;
import com.eduplatform.payment.model.PaymentStatus;
import com.eduplatform.payment.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Slf4j
@Service
public class PlatformStatisticsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentDisputeRepository disputeRepository;

    @Autowired
    private UserReportRepository userReportRepository;

    @Transactional(readOnly = true)
    public PlatformStatsResponse getPlatformStatistics() {
        try {
            PlatformStatsResponse stats = new PlatformStatsResponse();

            var allUsers = userRepository.findAll();
            stats.setTotalUsers(allUsers.size());
            stats.setTotalInstructors((int) allUsers.stream()
                    .filter(u -> "INSTRUCTOR".equalsIgnoreCase(u.getRole()))
                    .count());
            stats.setTotalStudents((int) allUsers.stream()
                    .filter(u -> "STUDENT".equalsIgnoreCase(u.getRole()))
                    .count());

            LocalDateTime monthStart = YearMonth.now().atDay(1).atStartOfDay();
            stats.setActiveUsersThisMonth((int) allUsers.stream()
                    .filter(u -> u.getLastLoginAt() != null && !u.getLastLoginAt().isBefore(monthStart))
                    .count());

            var courses = courseRepository.findAll();
            stats.setTotalCourses(courses.size());
            stats.setPublishedCourses((int) courses.stream()
                    .filter(c -> "PUBLISHED".equalsIgnoreCase(c.getStatus()))
                    .count());

            var enrollments = enrollmentRepository.findAll();
            stats.setTotalEnrollments(enrollments.size());
            stats.setCompletedCourses((int) enrollments.stream()
                    .filter(e -> e.getProgressPercentage() >= 100.0)
                    .count());

            if (!enrollments.isEmpty()) {
                double avgCompletion = (double) stats.getCompletedCourses() / stats.getTotalEnrollments() * 100;
                stats.setAverageCompletion(avgCompletion);
            }

            var payments = paymentRepository.findAll();
            BigDecimal totalRevenue = payments.stream()
                    .filter(p -> p.getStatus() == PaymentStatus.CAPTURED)
                    .map(p -> p.getAmount() == null ? BigDecimal.ZERO : p.getAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.setTotalRevenue(totalRevenue);

            BigDecimal monthlyRevenue = payments.stream()
                    .filter(p -> p.getStatus() == PaymentStatus.CAPTURED)
                    .filter(p -> p.getCapturedAt() != null && !p.getCapturedAt().isBefore(monthStart))
                    .map(p -> p.getAmount() == null ? BigDecimal.ZERO : p.getAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.setMonthlyRevenue(monthlyRevenue);

            double avgRating = courses.stream()
                    .mapToDouble(course -> course.getRating())
                    .average()
                    .orElse(0.0);
            stats.setAverageRating(avgRating);

            stats.setOpenDisputes(disputeRepository.findByStatusOrderByCreatedAtDesc(DisputeStatus.OPEN.name()).size());
            stats.setOpenReports(userReportRepository.findByStatusOrderByReportedAtDesc("OPEN").size());
            stats.setBannedUsers((int) allUsers.stream()
                    .filter(u -> "BANNED".equalsIgnoreCase(u.getStatus()))
                    .count());
            stats.setLastUpdated(LocalDateTime.now());

            return stats;
        } catch (Exception e) {
            log.error("Error calculating platform statistics", e);
            throw new RuntimeException("Failed to calculate statistics");
        }
    }
}
