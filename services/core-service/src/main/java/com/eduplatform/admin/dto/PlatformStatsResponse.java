package com.eduplatform.admin.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PlatformStatsResponse {
    private Integer totalUsers;
    private Integer totalInstructors;
    private Integer totalStudents;
    private Integer activeUsersThisMonth;
    private Integer totalCourses;
    private Integer publishedCourses;
    private Integer totalEnrollments;
    private Integer completedCourses;
    private Double averageCompletion;
    private BigDecimal totalRevenue;
    private BigDecimal monthlyRevenue;
    private Double averageRating;
    private Integer openDisputes;
    private Integer openReports;
    private Integer bannedUsers;
    private LocalDateTime lastUpdated;
}