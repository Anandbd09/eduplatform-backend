package com.eduplatform.social.service;

import com.eduplatform.social.model.UserFollow;
import com.eduplatform.social.repository.UserFollowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.Optional;

@Slf4j
@Service
public class FollowService {

    @Autowired
    private UserFollowRepository followRepository;

    /**
     * CHECK IF USER IS FOLLOWED
     */
    public boolean isFollowing(String followerId, String followingId, String tenantId) {
        try {
            Optional<UserFollow> follow = followRepository
                    .findByFollowerIdAndFollowingIdAndTenantId(followerId, followingId, tenantId);

            return follow.isPresent() && follow.get().isActive();
        } catch (Exception e) {
            log.warn("Error checking follow status", e);
            return false;
        }
    }

    /**
     * BLOCK USER
     */
    public void blockUser(String userId, String blockedUserId, String tenantId) {
        try {
            Optional<UserFollow> follow = followRepository
                    .findByFollowerIdAndFollowingIdAndTenantId(userId, blockedUserId, tenantId);

            if (follow.isPresent()) {
                UserFollow f = follow.get();
                f.setStatus("BLOCKED");
                followRepository.save(f);
            }

            log.info("User blocked: blocker={}, blocked={}", userId, blockedUserId);
        } catch (Exception e) {
            log.error("Error blocking user", e);
        }
    }
}