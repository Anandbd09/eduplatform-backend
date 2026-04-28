// FILE 9: ForumPostRepository.java
package com.eduplatform.social.repository;

import com.eduplatform.social.model.ForumPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ForumPostRepository extends MongoRepository<ForumPost, String> {

    Page<ForumPost> findByThreadIdAndTenantId(String threadId, String tenantId, Pageable pageable);

    Page<ForumPost> findByAuthorIdAndTenantId(String authorId, String tenantId, Pageable pageable);

    Long countByThreadIdAndTenantId(String threadId, String tenantId);
}