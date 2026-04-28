package com.eduplatform.referral.service;

import com.eduplatform.referral.model.ReferralClick;
import com.eduplatform.referral.repository.ReferralClickRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
public class TrackingService {

    @Autowired
    private ReferralClickRepository clickRepository;

    /**
     * MARK CLICK AS CONVERTED
     */
    public void markAsConverted(String clickId, String orderId, String courseId) {
        try {
            var click = clickRepository.findById(clickId);
            if (click.isPresent()) {
                ReferralClick c = click.get();
                c.setStatus("CONVERTED");
                c.setConvertedAt(LocalDateTime.now());
                c.setOrderId(orderId);
                c.setCourseId(courseId);
                clickRepository.save(c);
                log.info("Click marked as converted: clickId={}, orderId={}", clickId, orderId);
            }
        } catch (Exception e) {
            log.error("Error marking click as converted", e);
        }
    }

    /**
     * EXPIRE OLD CLICKS (30 DAYS)
     */
    public void expireOldClicks(String tenantId) {
        try {
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            List<ReferralClick> expiredClicks = clickRepository
                    .findByStatusAndClickedAtBetweenAndTenantId("PENDING",
                            thirtyDaysAgo.minusSeconds(1),
                            thirtyDaysAgo,
                            tenantId);

            expiredClicks.forEach(c -> {
                c.setStatus("EXPIRED");
                clickRepository.save(c);
                log.info("Click expired: id={}", c.getId());
            });

        } catch (Exception e) {
            log.error("Error expiring old clicks", e);
        }
    }
}