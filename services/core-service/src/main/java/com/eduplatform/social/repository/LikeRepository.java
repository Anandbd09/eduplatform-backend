// FILE 10: LikeRepository.java
package com.eduplatform.social.repository;

import com.eduplatform.social.model.Like;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LikeRepository extends MongoRepository<Like, String> {

    Optional<Like> findByUserIdAndContentIdAndTenantId(String userId, String contentId, String tenantId);

    Long countByContentIdAndTenantId(String contentId, String tenantId);
}