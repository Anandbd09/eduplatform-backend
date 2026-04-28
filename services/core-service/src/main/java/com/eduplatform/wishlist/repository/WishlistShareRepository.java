package com.eduplatform.wishlist.repository;

import com.eduplatform.wishlist.model.WishlistShare;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WishlistShareRepository extends MongoRepository<WishlistShare, String> {

    /**
     * Find share by collection and shared user
     */
    Optional<WishlistShare> findByCollectionIdAndSharedWith(String collectionId, String sharedWith);

    /**
     * Find shares sent by user
     */
    Page<WishlistShare> findBySharedByAndTenantId(String sharedBy, String tenantId, Pageable pageable);

    /**
     * Find shares received by user
     */
    Page<WishlistShare> findBySharedWithAndTenantId(String sharedWith, String tenantId, Pageable pageable);

    /**
     * Find share by token
     */
    Optional<WishlistShare> findByShareToken(String shareToken);
}