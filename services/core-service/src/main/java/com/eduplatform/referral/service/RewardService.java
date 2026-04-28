package com.eduplatform.referral.service;

import com.eduplatform.referral.model.ReferralReward;
import com.eduplatform.referral.repository.ReferralRewardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
public class RewardService {

    @Autowired
    private ReferralRewardRepository rewardRepository;

    private static final Double REWARD_PERCENTAGE = 0.20; // 20% of course price

    /**
     * CALCULATE REWARD AMOUNT (20% OF COURSE PRICE)
     */
    public Double calculateReward(Double coursePrice) {
        if (coursePrice == null || coursePrice <= 0) {
            return 0.0;
        }
        return coursePrice * REWARD_PERCENTAGE;
    }

    /**
     * APPROVE REWARDS (CALLED BY ADMIN)
     */
    public void approveRewards(List<String> rewardIds) {
        try {
            rewardIds.forEach(rewardId -> {
                var reward = rewardRepository.findById(rewardId);
                if (reward.isPresent()) {
                    ReferralReward r = reward.get();
                    r.setStatus("APPROVED");
                    r.setApprovedAt(LocalDateTime.now());
                    rewardRepository.save(r);
                    log.info("Reward approved: id={}", rewardId);
                }
            });
        } catch (Exception e) {
            log.error("Error approving rewards", e);
        }
    }

    /**
     * EXPIRE OLD REWARDS (30 DAYS)
     */
    public void expireOldRewards(String tenantId) {
        try {
            List<ReferralReward> expiredRewards = rewardRepository
                    .findByStatusAndExpiresAtBeforeAndTenantId("PENDING", LocalDateTime.now(), tenantId);

            expiredRewards.forEach(r -> {
                r.setStatus("EXPIRED");
                rewardRepository.save(r);
                log.info("Reward expired: id={}", r.getId());
            });

        } catch (Exception e) {
            log.error("Error expiring old rewards", e);
        }
    }
}