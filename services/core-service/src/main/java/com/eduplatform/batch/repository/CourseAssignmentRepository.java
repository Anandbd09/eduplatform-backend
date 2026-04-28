// FILE 10: CourseAssignmentRepository.java
package com.eduplatform.batch.repository;

import com.eduplatform.batch.model.CourseAssignment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CourseAssignmentRepository extends MongoRepository<CourseAssignment, String> {

    Optional<CourseAssignment> findByJobIdAndTenantId(String jobId, String tenantId);
}