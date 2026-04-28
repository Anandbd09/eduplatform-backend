package com.eduplatform.wishlist.repository;

import com.eduplatform.wishlist.model.Wishlist;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WishlistRepository extends MongoRepository<Wishlist, String> {

    /**
     * Find wishlist by user ID and tenant
     */
    Optional<Wishlist> findByUserIdAndTenantId(String userId, String tenantId);

    /**
     * Check if wishlist exists for user
     */
    boolean existsByUserIdAndTenantId(String userId, String tenantId);
}