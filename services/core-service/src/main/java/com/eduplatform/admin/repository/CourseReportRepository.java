package com.eduplatform.admin.repository;

import com.eduplatform.admin.model.CourseReport;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseReportRepository extends MongoRepository<CourseReport, String> {
    List<CourseReport> findByCourseId(String courseId);

    List<CourseReport> findByStatusOrderByReportedAtDesc(String status);

    List<CourseReport> findByInstructorId(String instructorId);

    long countByCourseIdAndStatus(String courseId, String status);
}