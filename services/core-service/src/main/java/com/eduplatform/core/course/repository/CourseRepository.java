package com.eduplatform.core.course.repository;

import com.eduplatform.core.course.model.Course;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends MongoRepository<Course, String> {

    Optional<Course> findByIdAndTenantId(String id, String tenantId);

    List<Course> findByInstructorIdAndTenantId(String instructorId, String tenantId);

    List<Course> findByStatusAndTenantId(String status, String tenantId);

    @Query("{ 'tenantId': ?0, 'status': 'PUBLISHED', 'title': { $regex: ?1, $options: 'i' } }")
    List<Course> searchPublishedCourses(String tenantId, String query);

    List<Course> findByCategoryAndStatusAndTenantId(String category, String status, String tenantId);
}