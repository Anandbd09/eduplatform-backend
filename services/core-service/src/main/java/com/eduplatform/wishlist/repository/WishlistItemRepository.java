package com.eduplatform.wishlist.repository;

import com.eduplatform.wishlist.model.WishlistItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistItemRepository extends MongoRepository<WishlistItem, String> {

    /**
     * Find item by user and course
     */
    Optional<WishlistItem> findByUserIdAndCourseIdAndTenantId(String userId, String courseId, String tenantId);

    /**
     * Find all items for a user
     */
    Page<WishlistItem> findByUserIdAndTenantId(String userId, String tenantId, Pageable pageable);

    /**
     * Find favorite items
     */
    Page<WishlistItem> findByUserIdAndIsFavoriteTrueAndTenantId(String userId, String tenantId, Pageable pageable);

    /**
     * Count wishlist items for user
     */
    Long countByUserIdAndTenantId(String userId, String tenantId);

    /**
     * Count favorite items
     */
    Long countByUserIdAndIsFavoriteTrueAndTenantId(String userId, String tenantId);

    /**
     * Check if item exists
     */
    boolean existsByUserIdAndCourseIdAndTenantId(String userId, String courseId, String tenantId);

    /**
     * Find items in collection
     */
    @Query("{ 'userId': ?0, 'collectionIds': ?1, 'tenantId': ?2 }")
    Page<WishlistItem> findItemsInCollection(String userId, String collectionId, String tenantId, Pageable pageable);
}