package com.eduplatform.wishlist.service;

import com.eduplatform.wishlist.model.Wishlist;
import com.eduplatform.wishlist.model.WishlistItem;
import com.eduplatform.wishlist.dto.WishlistResponse;
import com.eduplatform.wishlist.dto.WishlistItemResponse;
import com.eduplatform.wishlist.dto.WishlistStatistics;
import com.eduplatform.wishlist.repository.WishlistRepository;
import com.eduplatform.wishlist.repository.WishlistItemRepository;
import com.eduplatform.wishlist.exception.WishlistException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private WishlistItemRepository wishlistItemRepository;

    /**
     * Get or create user wishlist
     */
    public WishlistResponse getUserWishlist(String userId, String userName, String userEmail, String tenantId) {

        try {
            Wishlist wishlist = wishlistRepository.findByUserIdAndTenantId(userId, tenantId)
                    .orElseGet(() -> {
                        Wishlist newWishlist = Wishlist.builder()
                                .id(UUID.randomUUID().toString())
                                .userId(userId)
                                .userName(userName)
                                .userEmail(userEmail)
                                .totalItems(0)
                                .totalCollections(0)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .tenantId(tenantId)
                                .version(0L)
                                .build();
                        return wishlistRepository.save(newWishlist);
                    });

            return convertToResponse(wishlist);

        } catch (Exception e) {
            log.error("Error getting wishlist", e);
            throw new WishlistException("Failed to get wishlist");
        }
    }

    /**
     * Add course to wishlist
     */
    public WishlistItemResponse addToWishlist(String userId, String courseId,
                                              String courseName, String courseImage,
                                              String courseDescription, Double coursePrice,
                                              Double courseRating, String instructorName,
                                              String instructorId, String tenantId) {

        try {
            // Check if already in wishlist
            if (wishlistItemRepository.existsByUserIdAndCourseIdAndTenantId(userId, courseId, tenantId)) {
                throw new WishlistException("Course already in wishlist");
            }

            WishlistItem item = WishlistItem.builder()
                    .id(UUID.randomUUID().toString())
                    .userId(userId)
                    .courseId(courseId)
                    .courseName(courseName)
                    .courseImage(courseImage)
                    .courseDescription(courseDescription)
                    .coursePrice(coursePrice)
                    .courseRating(courseRating)
                    .instructorName(instructorName)
                    .instructorId(instructorId)
                    .isFavorite(false)
                    .addedAt(LocalDateTime.now())
                    .tenantId(tenantId)
                    .version(0L)
                    .build();

            WishlistItem saved = wishlistItemRepository.save(item);

            // Update wishlist count
            updateWishlistItemCount(userId, tenantId);

            log.info("Course added to wishlist: {} by user: {}", courseId, userId);
            return convertItemToResponse(saved);

        } catch (WishlistException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error adding to wishlist", e);
            throw new WishlistException("Failed to add to wishlist");
        }
    }

    /**
     * Remove from wishlist
     */
    public void removeFromWishlist(String userId, String courseId, String tenantId) {

        try {
            WishlistItem item = wishlistItemRepository.findByUserIdAndCourseIdAndTenantId(userId, courseId, tenantId)
                    .orElseThrow(() -> new WishlistException("Item not in wishlist"));

            wishlistItemRepository.deleteById(item.getId());

            // Update wishlist count
            updateWishlistItemCount(userId, tenantId);

            log.info("Course removed from wishlist: {} by user: {}", courseId, userId);

        } catch (WishlistException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error removing from wishlist", e);
            throw new WishlistException("Failed to remove from wishlist");
        }
    }

    /**
     * Get wishlist items
     */
    public Page<WishlistItemResponse> getWishlistItems(String userId, int page, int size, String tenantId) {

        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);

            Pageable pageable = PageRequest.of(page, size, Sort.by("addedAt").descending());
            Page<WishlistItem> items = wishlistItemRepository.findByUserIdAndTenantId(userId, tenantId, pageable);

            return items.map(this::convertItemToResponse);

        } catch (Exception e) {
            log.error("Error fetching wishlist items", e);
            throw new WishlistException("Failed to fetch wishlist items");
        }
    }

    /**
     * Get favorite items
     */
    public Page<WishlistItemResponse> getFavorites(String userId, int page, int size, String tenantId) {

        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);

            Pageable pageable = PageRequest.of(page, size, Sort.by("markedFavoriteAt").descending());
            Page<WishlistItem> items = wishlistItemRepository.findByUserIdAndIsFavoriteTrueAndTenantId(userId, tenantId, pageable);

            return items.map(this::convertItemToResponse);

        } catch (Exception e) {
            log.error("Error fetching favorites", e);
            throw new WishlistException("Failed to fetch favorites");
        }
    }

    /**
     * Mark as favorite
     */
    public void markAsFavorite(String userId, String courseId, String tenantId) {

        try {
            WishlistItem item = wishlistItemRepository.findByUserIdAndCourseIdAndTenantId(userId, courseId, tenantId)
                    .orElseThrow(() -> new WishlistException("Item not in wishlist"));

            item.markAsFavorite();
            wishlistItemRepository.save(item);

            log.info("Course marked as favorite: {} by user: {}", courseId, userId);

        } catch (WishlistException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error marking as favorite", e);
            throw new WishlistException("Failed to mark as favorite");
        }
    }

    /**
     * Unmark as favorite
     */
    public void unmarkAsFavorite(String userId, String courseId, String tenantId) {

        try {
            WishlistItem item = wishlistItemRepository.findByUserIdAndCourseIdAndTenantId(userId, courseId, tenantId)
                    .orElseThrow(() -> new WishlistException("Item not in wishlist"));

            item.unmarkAsFavorite();
            wishlistItemRepository.save(item);

            log.info("Course unmarked as favorite: {} by user: {}", courseId, userId);

        } catch (WishlistException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error unmarking as favorite", e);
            throw new WishlistException("Failed to unmark as favorite");
        }
    }

    /**
     * Get statistics
     */
    public WishlistStatistics getStatistics(String userId, String tenantId) {

        try {
            Long totalItems = wishlistItemRepository.countByUserIdAndTenantId(userId, tenantId);
            Long favoriteItems = wishlistItemRepository.countByUserIdAndIsFavoriteTrueAndTenantId(userId, tenantId);

            return WishlistStatistics.builder()
                    .totalItems(totalItems)
                    .favoriteItems(favoriteItems)
                    .nonFavoriteItems(totalItems - favoriteItems)
                    .build();

        } catch (Exception e) {
            log.error("Error getting statistics", e);
            throw new WishlistException("Failed to get statistics");
        }
    }

    /**
     * Update wishlist item count
     */
    private void updateWishlistItemCount(String userId, String tenantId) {
        Wishlist wishlist = wishlistRepository.findByUserIdAndTenantId(userId, tenantId)
                .orElse(null);

        if (wishlist != null) {
            Long count = wishlistItemRepository.countByUserIdAndTenantId(userId, tenantId);
            wishlist.setTotalItems(count.intValue());
            wishlistRepository.save(wishlist);
        }
    }

    /**
     * Convert to response
     */
    private WishlistResponse convertToResponse(Wishlist wishlist) {
        if (wishlist == null) return null;

        return WishlistResponse.builder()
                .id(wishlist.getId())
                .userId(wishlist.getUserId())
                .userName(wishlist.getUserName())
                .totalItems(wishlist.getTotalItems())
                .totalCollections(wishlist.getTotalCollections())
                .createdAt(wishlist.getCreatedAt())
                .updatedAt(wishlist.getUpdatedAt())
                .build();
    }

    /**
     * Convert item to response
     */
    private WishlistItemResponse convertItemToResponse(WishlistItem item) {
        if (item == null) return null;

        return WishlistItemResponse.builder()
                .id(item.getId())
                .courseId(item.getCourseId())
                .courseName(item.getCourseName())
                .courseImage(item.getCourseImage())
                .courseDescription(item.getCourseDescription())
                .coursePrice(item.getCoursePrice())
                .courseRating(item.getCourseRating())
                .instructorName(item.getInstructorName())
                .isFavorite(item.getIsFavorite())
                .addedAt(item.getAddedAt())
                .markedFavoriteAt(item.getMarkedFavoriteAt())
                .collectionCount(item.getCollectionIds() != null ? item.getCollectionIds().size() : 0)
                .build();
    }
}