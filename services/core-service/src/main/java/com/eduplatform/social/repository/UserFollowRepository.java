// FILE 6: UserFollowRepository.java
package com.eduplatform.social.repository;

import com.eduplatform.social.model.UserFollow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface UserFollowRepository extends MongoRepository<UserFollow, String> {

    Optional<UserFollow> findByFollowerIdAndFollowingIdAndTenantId(String followerId, String followingId, String tenantId);

    Page<UserFollow> findByFollowerIdAndStatusAndTenantId(String followerId, String status, String tenantId, Pageable pageable);

    Page<UserFollow> findByFollowingIdAndStatusAndTenantId(String followingId, String status, String tenantId, Pageable pageable);

    Long countByFollowerIdAndStatusAndTenantId(String followerId, String status, String tenantId);

    Long countByFollowingIdAndStatusAndTenantId(String followingId, String status, String tenantId);
}