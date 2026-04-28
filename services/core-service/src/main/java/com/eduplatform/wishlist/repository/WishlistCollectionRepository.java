package com.eduplatform.wishlist.repository;

import com.eduplatform.wishlist.model.WishlistCollection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WishlistCollectionRepository extends MongoRepository<WishlistCollection, String> {

    /**
     * Find collection by user and name
     */
    Optional<WishlistCollection> findByUserIdAndNameAndTenantId(String userId, String name, String tenantId);

    /**
     * Find all collections for a user
     */
    Page<WishlistCollection> findByUserIdAndTenantId(String userId, String tenantId, Pageable pageable);

    /**
     * Find public collections
     */
    Page<WishlistCollection> findByVisibilityAndTenantId(String visibility, String tenantId, Pageable pageable);

    /**
     * Count collections for user
     */
    Long countByUserIdAndTenantId(String userId, String tenantId);

    /**
     * Check if collection exists
     */
    boolean existsByIdAndUserId(String collectionId, String userId);
}