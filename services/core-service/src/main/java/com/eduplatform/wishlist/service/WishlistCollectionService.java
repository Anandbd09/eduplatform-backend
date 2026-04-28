package com.eduplatform.wishlist.service;

import com.eduplatform.wishlist.dto.WishlistItemResponse;
import com.eduplatform.wishlist.model.WishlistCollection;
import com.eduplatform.wishlist.model.WishlistItem;
import com.eduplatform.wishlist.dto.WishlistCollectionResponse;
import com.eduplatform.wishlist.repository.WishlistCollectionRepository;
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
public class WishlistCollectionService {

    @Autowired
    private WishlistCollectionRepository collectionRepository;

    @Autowired
    private WishlistItemRepository wishlistItemRepository;

    /**
     * Create collection
     */
    public WishlistCollectionResponse createCollection(String userId, String name, String description,
                                                       String visibility, String tenantId) {

        try {
            // Check if name already exists
            if (collectionRepository.findByUserIdAndNameAndTenantId(userId, name, tenantId).isPresent()) {
                throw new WishlistException("Collection name already exists");
            }

            WishlistCollection collection = WishlistCollection.builder()
                    .id(UUID.randomUUID().toString())
                    .userId(userId)
                    .name(name)
                    .description(description)
                    .visibility(visibility != null ? visibility : "PRIVATE")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .courseCount(0)
                    .tenantId(tenantId)
                    .version(0L)
                    .build();

            WishlistCollection saved = collectionRepository.save(collection);
            log.info("Collection created: {} by user: {}", name, userId);

            return convertToResponse(saved);

        } catch (WishlistException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating collection", e);
            throw new WishlistException("Failed to create collection");
        }
    }

    /**
     * Get user collections
     */
    public Page<WishlistCollectionResponse> getUserCollections(String userId, int page, int size, String tenantId) {

        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);

            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<WishlistCollection> collections = collectionRepository.findByUserIdAndTenantId(userId, tenantId, pageable);

            return collections.map(this::convertToResponse);

        } catch (Exception e) {
            log.error("Error fetching collections", e);
            throw new WishlistException("Failed to fetch collections");
        }
    }

    /**
     * Update collection
     */
    public WishlistCollectionResponse updateCollection(String collectionId, String userId,
                                                       String name, String description,
                                                       String visibility, String tenantId) {

        try {
            WishlistCollection collection = collectionRepository.findById(collectionId)
                    .orElseThrow(() -> new WishlistException("Collection not found"));

            // Check ownership
            if (!collection.getUserId().equals(userId)) {
                throw new WishlistException("You can only edit your own collections");
            }

            if (name != null && !name.isEmpty()) {
                collection.setName(name);
            }
            if (description != null) {
                collection.setDescription(description);
            }
            if (visibility != null) {
                collection.setVisibility(visibility);
            }

            collection.setUpdatedAt(LocalDateTime.now());
            collection.setVersion(collection.getVersion() + 1);

            WishlistCollection updated = collectionRepository.save(collection);
            log.info("Collection updated: {}", collectionId);

            return convertToResponse(updated);

        } catch (WishlistException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating collection", e);
            throw new WishlistException("Failed to update collection");
        }
    }

    /**
     * Delete collection
     */
    public void deleteCollection(String collectionId, String userId, String tenantId) {

        try {
            WishlistCollection collection = collectionRepository.findById(collectionId)
                    .orElseThrow(() -> new WishlistException("Collection not found"));

            // Check ownership
            if (!collection.getUserId().equals(userId)) {
                throw new WishlistException("You can only delete your own collections");
            }

            collectionRepository.deleteById(collectionId);
            log.info("Collection deleted: {}", collectionId);

        } catch (WishlistException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting collection", e);
            throw new WishlistException("Failed to delete collection");
        }
    }

    /**
     * Add course to collection
     */
    public void addCourseToCollection(String collectionId, String userId, String courseId, String tenantId) {

        try {
            WishlistCollection collection = collectionRepository.findById(collectionId)
                    .orElseThrow(() -> new WishlistException("Collection not found"));

            // Check ownership
            if (!collection.getUserId().equals(userId)) {
                throw new WishlistException("You can only edit your own collections");
            }

            // Check if item exists in wishlist
            WishlistItem item = wishlistItemRepository.findByUserIdAndCourseIdAndTenantId(userId, courseId, tenantId)
                    .orElseThrow(() -> new WishlistException("Course not in wishlist"));

            // Add to collection
            collection.addCourse(courseId);
            item.addToCollection(collectionId);

            collectionRepository.save(collection);
            wishlistItemRepository.save(item);

            log.info("Course added to collection: {} by user: {}", collectionId, userId);

        } catch (WishlistException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error adding course to collection", e);
            throw new WishlistException("Failed to add course to collection");
        }
    }

    /**
     * Remove course from collection
     */
    public void removeCourseFromCollection(String collectionId, String userId, String courseId, String tenantId) {

        try {
            WishlistCollection collection = collectionRepository.findById(collectionId)
                    .orElseThrow(() -> new WishlistException("Collection not found"));

            // Check ownership
            if (!collection.getUserId().equals(userId)) {
                throw new WishlistException("You can only edit your own collections");
            }

            WishlistItem item = wishlistItemRepository.findByUserIdAndCourseIdAndTenantId(userId, courseId, tenantId)
                    .orElseThrow(() -> new WishlistException("Course not in wishlist"));

            // Remove from collection
            collection.removeCourse(courseId);
            item.removeFromCollection(collectionId);

            collectionRepository.save(collection);
            wishlistItemRepository.save(item);

            log.info("Course removed from collection: {} by user: {}", collectionId, userId);

        } catch (WishlistException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error removing course from collection", e);
            throw new WishlistException("Failed to remove course from collection");
        }
    }

    /**
     * Get collection items
     */
    public Page<WishlistItemResponse> getCollectionItems(String collectionId, String userId,
                                                         int page, int size, String tenantId) {

        try {
            WishlistCollection collection = collectionRepository.findById(collectionId)
                    .orElseThrow(() -> new WishlistException("Collection not found"));

            // Check access (owner or shared)
            if (!collection.getUserId().equals(userId) && collection.isPrivate()) {
                throw new WishlistException("You don't have access to this collection");
            }

            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);

            Pageable pageable = PageRequest.of(page, size);
            Page<WishlistItem> items = wishlistItemRepository.findItemsInCollection(userId, collectionId, tenantId, pageable);

            return items.map(this::convertItemToResponse);

        } catch (WishlistException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching collection items", e);
            throw new WishlistException("Failed to fetch collection items");
        }
    }

    /**
     * Convert to response
     */
    private WishlistCollectionResponse convertToResponse(WishlistCollection collection) {
        if (collection == null) return null;

        return WishlistCollectionResponse.builder()
                .id(collection.getId())
                .userId(collection.getUserId())
                .name(collection.getName())
                .description(collection.getDescription())
                .visibility(collection.getVisibility())
                .courseCount(collection.getCourseCount())
                .createdAt(collection.getCreatedAt())
                .updatedAt(collection.getUpdatedAt())
                .isPrivate(collection.isPrivate())
                .isShared(collection.isShared())
                .isPublic(collection.isPublic())
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