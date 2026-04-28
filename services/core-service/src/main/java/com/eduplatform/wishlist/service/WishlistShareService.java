package com.eduplatform.wishlist.service;

import com.eduplatform.wishlist.model.WishlistShare;
import com.eduplatform.wishlist.dto.WishlistShareResponse;
import com.eduplatform.wishlist.repository.WishlistShareRepository;
import com.eduplatform.wishlist.repository.WishlistCollectionRepository;
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
public class WishlistShareService {

    @Autowired
    private WishlistShareRepository shareRepository;

    @Autowired
    private WishlistCollectionRepository collectionRepository;

    /**
     * Share collection
     */
    public WishlistShareResponse shareCollection(String collectionId, String userId,
                                                 String sharedWithEmail, String tenantId) {

        try {
            var collection = collectionRepository.findById(collectionId)
                    .orElseThrow(() -> new WishlistException("Collection not found"));

            // Check ownership
            if (!collection.getUserId().equals(userId)) {
                throw new WishlistException("You can only share your own collections");
            }

            // Check if already shared with this email
            var existingShare = shareRepository.findByCollectionIdAndSharedWith(collectionId, sharedWithEmail);
            if (existingShare.isPresent()) {
                throw new WishlistException("Collection already shared with this user");
            }

            WishlistShare share = WishlistShare.builder()
                    .id(UUID.randomUUID().toString())
                    .collectionId(collectionId)
                    .sharedBy(userId)
                    .sharedWith(sharedWithEmail)
                    .shareToken(UUID.randomUUID().toString())
                    .sharedAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusDays(30)) // 30 day expiry
                    .isActive(true)
                    .tenantId(tenantId)
                    .build();

            WishlistShare saved = shareRepository.save(share);
            log.info("Collection shared: {} by user: {}", collectionId, userId);

            return convertToResponse(saved);

        } catch (WishlistException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error sharing collection", e);
            throw new WishlistException("Failed to share collection");
        }
    }

    /**
     * Get shares sent by user
     */
    public Page<WishlistShareResponse> getOutgoingShares(String userId, int page, int size, String tenantId) {

        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);

            Pageable pageable = PageRequest.of(page, size, Sort.by("sharedAt").descending());
            Page<WishlistShare> shares = shareRepository.findBySharedByAndTenantId(userId, tenantId, pageable);

            return shares.map(this::convertToResponse);

        } catch (Exception e) {
            log.error("Error fetching outgoing shares", e);
            throw new WishlistException("Failed to fetch shares");
        }
    }

    /**
     * Get shares received by user
     */
    public Page<WishlistShareResponse> getIncomingShares(String userId, int page, int size, String tenantId) {

        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);

            Pageable pageable = PageRequest.of(page, size, Sort.by("sharedAt").descending());
            Page<WishlistShare> shares = shareRepository.findBySharedWithAndTenantId(userId, tenantId, pageable);

            return shares.map(this::convertToResponse);

        } catch (Exception e) {
            log.error("Error fetching incoming shares", e);
            throw new WishlistException("Failed to fetch shares");
        }
    }

    /**
     * Revoke share
     */
    public void revokeShare(String shareId, String userId, String tenantId) {

        try {
            WishlistShare share = shareRepository.findById(shareId)
                    .orElseThrow(() -> new WishlistException("Share not found"));

            // Check ownership
            if (!share.getSharedBy().equals(userId)) {
                throw new WishlistException("You can only revoke your own shares");
            }

            shareRepository.deleteById(shareId);
            log.info("Share revoked: {}", shareId);

        } catch (WishlistException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error revoking share", e);
            throw new WishlistException("Failed to revoke share");
        }
    }

    /**
     * Access shared collection
     */
    public String accessSharedCollection(String shareToken) {

        try {
            WishlistShare share = shareRepository.findByShareToken(shareToken)
                    .orElseThrow(() -> new WishlistException("Share token not found or expired"));

            // Check if share is active and not expired
            if (!share.getIsActive() || share.isExpired()) {
                throw new WishlistException("Share has expired");
            }

            return share.getCollectionId();

        } catch (WishlistException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error accessing shared collection", e);
            throw new WishlistException("Failed to access shared collection");
        }
    }

    /**
     * Convert to response
     */
    private WishlistShareResponse convertToResponse(WishlistShare share) {
        if (share == null) return null;

        return WishlistShareResponse.builder()
                .id(share.getId())
                .collectionId(share.getCollectionId())
                .sharedBy(share.getSharedBy())
                .sharedWith(share.getSharedWith())
                .shareToken(share.getShareToken())
                .sharedAt(share.getSharedAt())
                .expiresAt(share.getExpiresAt())
                .isActive(share.getIsActive())
                .isExpired(share.isExpired())
                .build();
    }
}