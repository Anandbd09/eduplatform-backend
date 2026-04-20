package com.eduplatform.core.enrollment.repository;

import com.eduplatform.core.enrollment.model.Enrollment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends MongoRepository<Enrollment, String> {

    Optional<Enrollment> findByUserIdAndCourseIdAndTenantId(String userId, String courseId, String tenantId);

    List<Enrollment> findByUserIdAndTenantId(String userId, String tenantId);

    List<Enrollment> findByCourseIdAndTenantId(String courseId, String tenantId);

    List<Enrollment> findByUserIdAndStatusAndTenantId(String userId, String status, String tenantId);

    long countByCourseIdAndStatusAndTenantId(String courseId, String status, String tenantId);
}