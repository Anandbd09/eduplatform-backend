// FILE 8: ForumThreadRepository.java
package com.eduplatform.social.repository;

import com.eduplatform.social.model.ForumThread;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ForumThreadRepository extends MongoRepository<ForumThread, String> {

    Page<ForumThread> findByCourseIdAndStatusAndTenantId(String courseId, String status, String tenantId, Pageable pageable);

    Page<ForumThread> findByCourseIdAndCategoryAndStatusAndTenantId(String courseId, String category, String status, String tenantId, Pageable pageable);

    Long countByCourseIdAndStatusAndTenantId(String courseId, String status, String tenantId);
}