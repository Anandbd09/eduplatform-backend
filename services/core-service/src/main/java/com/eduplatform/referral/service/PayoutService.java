package com.eduplatform.referral.service;

import com.eduplatform.referral.model.ReferralPayout;
import com.eduplatform.referral.model.ReferralReward;
import com.eduplatform.referral.repository.ReferralPayoutRepository;
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
public class PayoutService {

    @Autowired
    private ReferralPayoutRepository payoutRepository;

    @Autowired
    private ReferralRewardRepository rewardRepository;

    /**
     * APPROVE PAYOUT (ADMIN)
     */
    public void approvePayout(String payoutId) {
        try {
            var payout = payoutRepository.findById(payoutId);
            if (payout.isPresent()) {
                ReferralPayout p = payout.get();
                p.setStatus("APPROVED");
                p.setApprovedAt(LocalDateTime.now());
                payoutRepository.save(p);
                log.info("Payout approved: id={}, amount={}", payoutId, p.getTotalAmount());
            }
        } catch (Exception e) {
            log.error("Error approving payout", e);
        }
    }

    /**
     * MARK PAYOUT AS PAID
     */
    public void markAsPaid(String payoutId, String transactionId) {
        try {
            var payout = payoutRepository.findById(payoutId);
            if (payout.isPresent()) {
                ReferralPayout p = payout.get();
                p.setStatus("PAID");
                p.setPaidAt(LocalDateTime.now());
                p.setTransactionId(transactionId);
                payoutRepository.save(p);

                // Mark all related rewards as PAID
                p.getRewardIds().forEach(rewardId -> {
                    var reward = rewardRepository.findById(rewardId);
                    if (reward.isPresent()) {
                        ReferralReward r = reward.get();
                        r.setStatus("PAID");
                        r.setPaidAt(LocalDateTime.now());
                        rewardRepository.save(r);
                    }
                });

                log.info("Payout marked as paid: id={}, transactionId={}", payoutId, transactionId);
            }
        } catch (Exception e) {
            log.error("Error marking payout as paid", e);
        }
    }
}