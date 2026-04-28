package com.eduplatform.wishlist.controller;

import com.eduplatform.wishlist.service.WishlistService;
import com.eduplatform.wishlist.service.WishlistCollectionService;
import com.eduplatform.wishlist.service.WishlistShareService;
import com.eduplatform.wishlist.dto.WishlistCollectionRequest;
import com.eduplatform.wishlist.exception.WishlistException;
import com.eduplatform.core.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private WishlistCollectionService collectionService;

    @Autowired
    private WishlistShareService shareService;

    // ============================================================
    // WISHLIST ENDPOINTS
    // ============================================================

    /**
     * GET WISHLIST
     * GET /api/v1/wishlist
     */
    @GetMapping
    public ResponseEntity<?> getWishlist(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Name", required = false) String userName,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            var wishlist = wishlistService.getUserWishlist(userId, userName, userEmail, tenantId);
            return ResponseEntity.ok(ApiResponse.success(wishlist, "Wishlist retrieved"));
        } catch (Exception e) {
            log.error("Error fetching wishlist", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch wishlist", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * ADD TO WISHLIST
     * POST /api/v1/wishlist/courses/{courseId}
     */
    @PostMapping("/courses/{courseId}")
    public ResponseEntity<?> addToWishlist(
            @PathVariable String courseId,
            @RequestParam String courseName,
            @RequestParam(required = false) String courseImage,
            @RequestParam(required = false) String courseDescription,
            @RequestParam(required = false) Double coursePrice,
            @RequestParam(required = false) Double courseRating,
            @RequestParam(required = false) String instructorName,
            @RequestParam(required = false) String instructorId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            var item = wishlistService.addToWishlist(userId, courseId, courseName, courseImage,
                    courseDescription, coursePrice, courseRating, instructorName, instructorId, tenantId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(item, "Added to wishlist"));
        } catch (WishlistException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "WISHLIST_ERROR"));
        } catch (Exception e) {
            log.error("Error adding to wishlist", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to add to wishlist", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * REMOVE FROM WISHLIST
     * DELETE /api/v1/wishlist/courses/{courseId}
     */
    @DeleteMapping("/courses/{courseId}")
    public ResponseEntity<?> removeFromWishlist(
            @PathVariable String courseId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            wishlistService.removeFromWishlist(userId, courseId, tenantId);
            return ResponseEntity.ok(ApiResponse.success(null, "Removed from wishlist"));
        } catch (WishlistException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "WISHLIST_ERROR"));
        } catch (Exception e) {
            log.error("Error removing from wishlist", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to remove from wishlist", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * GET WISHLIST ITEMS
     * GET /api/v1/wishlist/items?page=0&size=10
     */
    @GetMapping("/items")
    public ResponseEntity<?> getWishlistItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            Page<?> items = wishlistService.getWishlistItems(userId, page, size, tenantId);
            return ResponseEntity.ok(ApiResponse.success(items, "Items retrieved"));
        } catch (Exception e) {
            log.error("Error fetching items", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch items", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * GET FAVORITES
     * GET /api/v1/wishlist/favorites?page=0&size=10
     */
    @GetMapping("/favorites")
    public ResponseEntity<?> getFavorites(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            Page<?> favorites = wishlistService.getFavorites(userId, page, size, tenantId);
            return ResponseEntity.ok(ApiResponse.success(favorites, "Favorites retrieved"));
        } catch (Exception e) {
            log.error("Error fetching favorites", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch favorites", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * MARK AS FAVORITE
     * POST /api/v1/wishlist/courses/{courseId}/favorite
     */
    @PostMapping("/courses/{courseId}/favorite")
    public ResponseEntity<?> markAsFavorite(
            @PathVariable String courseId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            wishlistService.markAsFavorite(userId, courseId, tenantId);
            return ResponseEntity.ok(ApiResponse.success(null, "Marked as favorite"));
        } catch (WishlistException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "WISHLIST_ERROR"));
        } catch (Exception e) {
            log.error("Error marking as favorite", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to mark as favorite", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * UNMARK AS FAVORITE
     * DELETE /api/v1/wishlist/courses/{courseId}/favorite
     */
    @DeleteMapping("/courses/{courseId}/favorite")
    public ResponseEntity<?> unmarkAsFavorite(
            @PathVariable String courseId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            wishlistService.unmarkAsFavorite(userId, courseId, tenantId);
            return ResponseEntity.ok(ApiResponse.success(null, "Unmarked as favorite"));
        } catch (WishlistException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "WISHLIST_ERROR"));
        } catch (Exception e) {
            log.error("Error unmarking as favorite", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to unmark as favorite", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * GET STATISTICS
     * GET /api/v1/wishlist/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            var stats = wishlistService.getStatistics(userId, tenantId);
            return ResponseEntity.ok(ApiResponse.success(stats, "Statistics retrieved"));
        } catch (Exception e) {
            log.error("Error fetching statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch statistics", "INTERNAL_SERVER_ERROR"));
        }
    }

    // ============================================================
    // COLLECTION ENDPOINTS
    // ============================================================

    /**
     * CREATE COLLECTION
     * POST /api/v1/wishlist/collections
     */
    @PostMapping("/collections")
    public ResponseEntity<?> createCollection(
            @RequestBody WishlistCollectionRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            var collection = collectionService.createCollection(userId, request.getName(),
                    request.getDescription(), request.getVisibility(), tenantId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(collection, "Collection created"));
        } catch (WishlistException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "WISHLIST_ERROR"));
        } catch (Exception e) {
            log.error("Error creating collection", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create collection", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * GET COLLECTIONS
     * GET /api/v1/wishlist/collections?page=0&size=10
     */
    @GetMapping("/collections")
    public ResponseEntity<?> getCollections(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            Page<?> collections = collectionService.getUserCollections(userId, page, size, tenantId);
            return ResponseEntity.ok(ApiResponse.success(collections, "Collections retrieved"));
        } catch (Exception e) {
            log.error("Error fetching collections", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch collections", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * UPDATE COLLECTION
     * PUT /api/v1/wishlist/collections/{collectionId}
     */
    @PutMapping("/collections/{collectionId}")
    public ResponseEntity<?> updateCollection(
            @PathVariable String collectionId,
            @RequestBody WishlistCollectionRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            var collection = collectionService.updateCollection(collectionId, userId,
                    request.getName(), request.getDescription(), request.getVisibility(), tenantId);
            return ResponseEntity.ok(ApiResponse.success(collection, "Collection updated"));
        } catch (WishlistException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "WISHLIST_ERROR"));
        } catch (Exception e) {
            log.error("Error updating collection", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update collection", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * DELETE COLLECTION
     * DELETE /api/v1/wishlist/collections/{collectionId}
     */
    @DeleteMapping("/collections/{collectionId}")
    public ResponseEntity<?> deleteCollection(
            @PathVariable String collectionId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            collectionService.deleteCollection(collectionId, userId, tenantId);
            return ResponseEntity.ok(ApiResponse.success(null, "Collection deleted"));
        } catch (WishlistException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "WISHLIST_ERROR"));
        } catch (Exception e) {
            log.error("Error deleting collection", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete collection", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * ADD COURSE TO COLLECTION
     * POST /api/v1/wishlist/collections/{collectionId}/courses/{courseId}
     */
    @PostMapping("/collections/{collectionId}/courses/{courseId}")
    public ResponseEntity<?> addCourseToCollection(
            @PathVariable String collectionId,
            @PathVariable String courseId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            collectionService.addCourseToCollection(collectionId, userId, courseId, tenantId);
            return ResponseEntity.ok(ApiResponse.success(null, "Course added to collection"));
        } catch (WishlistException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "WISHLIST_ERROR"));
        } catch (Exception e) {
            log.error("Error adding course to collection", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to add course", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * REMOVE COURSE FROM COLLECTION
     * DELETE /api/v1/wishlist/collections/{collectionId}/courses/{courseId}
     */
    @DeleteMapping("/collections/{collectionId}/courses/{courseId}")
    public ResponseEntity<?> removeCourseFromCollection(
            @PathVariable String collectionId,
            @PathVariable String courseId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            collectionService.removeCourseFromCollection(collectionId, userId, courseId, tenantId);
            return ResponseEntity.ok(ApiResponse.success(null, "Course removed from collection"));
        } catch (WishlistException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "WISHLIST_ERROR"));
        } catch (Exception e) {
            log.error("Error removing course from collection", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to remove course", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * GET COLLECTION ITEMS
     * GET /api/v1/wishlist/collections/{collectionId}/items?page=0&size=10
     */
    @GetMapping("/collections/{collectionId}/items")
    public ResponseEntity<?> getCollectionItems(
            @PathVariable String collectionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            Page<?> items = collectionService.getCollectionItems(collectionId, userId, page, size, tenantId);
            return ResponseEntity.ok(ApiResponse.success(items, "Items retrieved"));
        } catch (WishlistException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "WISHLIST_ERROR"));
        } catch (Exception e) {
            log.error("Error fetching collection items", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch items", "INTERNAL_SERVER_ERROR"));
        }
    }

    // ============================================================
    // SHARE ENDPOINTS
    // ============================================================

    /**
     * SHARE COLLECTION
     * POST /api/v1/wishlist/collections/{collectionId}/share
     */
    @PostMapping("/collections/{collectionId}/share")
    public ResponseEntity<?> shareCollection(
            @PathVariable String collectionId,
            @RequestParam String sharedWithEmail,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            var share = shareService.shareCollection(collectionId, userId, sharedWithEmail, tenantId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(share, "Collection shared"));
        } catch (WishlistException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "WISHLIST_ERROR"));
        } catch (Exception e) {
            log.error("Error sharing collection", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to share collection", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * GET OUTGOING SHARES
     * GET /api/v1/wishlist/shares/outgoing?page=0&size=10
     */
    @GetMapping("/shares/outgoing")
    public ResponseEntity<?> getOutgoingShares(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            Page<?> shares = shareService.getOutgoingShares(userId, page, size, tenantId);
            return ResponseEntity.ok(ApiResponse.success(shares, "Shares retrieved"));
        } catch (Exception e) {
            log.error("Error fetching outgoing shares", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch shares", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * GET INCOMING SHARES
     * GET /api/v1/wishlist/shares/incoming?page=0&size=10
     */
    @GetMapping("/shares/incoming")
    public ResponseEntity<?> getIncomingShares(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            Page<?> shares = shareService.getIncomingShares(userId, page, size, tenantId);
            return ResponseEntity.ok(ApiResponse.success(shares, "Shares retrieved"));
        } catch (Exception e) {
            log.error("Error fetching incoming shares", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch shares", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * REVOKE SHARE
     * DELETE /api/v1/wishlist/shares/{shareId}
     */
    @DeleteMapping("/shares/{shareId}")
    public ResponseEntity<?> revokeShare(
            @PathVariable String shareId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            shareService.revokeShare(shareId, userId, tenantId);
            return ResponseEntity.ok(ApiResponse.success(null, "Share revoked"));
        } catch (WishlistException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "WISHLIST_ERROR"));
        } catch (Exception e) {
            log.error("Error revoking share", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to revoke share", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * ACCESS SHARED COLLECTION
     * GET /api/v1/wishlist/shared/{shareToken}
     */
    @GetMapping("/shared/{shareToken}")
    public ResponseEntity<?> accessSharedCollection(
            @PathVariable String shareToken) {

        try {
            var collectionId = shareService.accessSharedCollection(shareToken);
            return ResponseEntity.ok(ApiResponse.success(collectionId, "Access granted"));
        } catch (WishlistException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "WISHLIST_ERROR"));
        } catch (Exception e) {
            log.error("Error accessing shared collection", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to access shared collection", "INTERNAL_SERVER_ERROR"));
        }
    }
}
