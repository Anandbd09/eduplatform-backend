package com.eduplatform.payment.service;

import com.eduplatform.payment.model.Subscription;
import com.eduplatform.payment.model.SubscriptionStatus;
import com.eduplatform.payment.repository.SubscriptionRepository;
import com.eduplatform.payment.dto.SubscriptionResponse;
import com.eduplatform.core.user.repository.UserRepository;
import com.eduplatform.notification.service.EmailService;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class SubscriptionService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private RazorpayService razorpayService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    // Create Subscription
    @Transactional
    public SubscriptionResponse createSubscription(String userId, String planId, String planName, BigDecimal amount) {
        try {
            Map<String, String> notes = new HashMap<>();
            notes.put("userId", userId);
            notes.put("planId", planId);

            // Create Razorpay subscription
            var razorpaySubscription = razorpayService.createSubscription(
                    planId,
                    12,
                    notes
            );

            // Save subscription
            Subscription subscription = new Subscription();
            subscription.setId(UUID.randomUUID().toString());
            subscription.setUserId(userId);
            subscription.setPlanId(planId);
            subscription.setRazorpaySubscriptionId(razorpaySubscription.getString("id"));
            subscription.setPlanAmount(amount);
            subscription.setCurrency("INR");
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.setAutoRenewal(true);
            subscription.setIsActive(true);
            subscription.setStartDate(LocalDateTime.now());
            subscription.setCycleCount(0);
            subscription.setTotalPayments(0);

            subscriptionRepository.save(subscription);

            return convertToResponse(subscription);

        } catch (RazorpayException e) {
            log.error("Error creating subscription", e);
            throw new RuntimeException("Failed to create subscription");
        }
    }

    // Cancel Subscription
    @Transactional
    public void cancelSubscription(String subscriptionId, String reason) {
        try {
            Subscription subscription = subscriptionRepository.findById(subscriptionId)
                    .orElseThrow(() -> new RuntimeException("Subscription not found"));

            razorpayService.cancelSubscription(subscription.getRazorpaySubscriptionId(), reason);

            subscription.setStatus(SubscriptionStatus.CANCELLED);
            subscription.setIsActive(false);
            subscription.setCancelledAt(LocalDateTime.now());
            subscription.setCancellationReason(reason);

            subscriptionRepository.save(subscription);

            // Send cancellation email
            var user = userRepository.findById(subscription.getUserId()).orElse(null);
            if (user != null) {
                emailService.sendSubscriptionCancelledEmail(user, subscription);
            }

        } catch (RazorpayException e) {
            log.error("Error cancelling subscription", e);
            throw new RuntimeException("Failed to cancel subscription");
        }
    }

    // Pause Subscription
    @Transactional
    public void pauseSubscription(String subscriptionId) {
        try {
            Subscription subscription = subscriptionRepository.findById(subscriptionId)
                    .orElseThrow(() -> new RuntimeException("Subscription not found"));

            razorpayService.pauseSubscription(subscription.getRazorpaySubscriptionId());

            subscription.setStatus(SubscriptionStatus.PAUSED);
            subscription.setPausedAt(LocalDateTime.now());

            subscriptionRepository.save(subscription);

        } catch (RazorpayException e) {
            log.error("Error pausing subscription", e);
            throw new RuntimeException("Failed to pause subscription");
        }
    }

    // Resume Subscription (unpause)
    @Transactional
    public void resumeSubscription(String subscriptionId) {
        try {
            Subscription subscription = subscriptionRepository.findById(subscriptionId)
                    .orElseThrow(() -> new RuntimeException("Subscription not found"));

            // Call Razorpay to resume
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.setPausedAt(null);

            subscriptionRepository.save(subscription);

        } catch (Exception e) {
            log.error("Error resuming subscription", e);
            throw new RuntimeException("Failed to resume subscription");
        }
    }

    // Get Active Subscription
    public SubscriptionResponse getActiveSubscription(String userId) {
        Subscription subscription = subscriptionRepository.findByUserIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new RuntimeException("No active subscription found"));

        return convertToResponse(subscription);
    }

    // Get All Subscriptions
    public List<SubscriptionResponse> getAllSubscriptions(String userId) {
        List<Subscription> subscriptions = subscriptionRepository.findByUserId(userId);
        return subscriptions.stream()
                .map(this::convertToResponse)
                .toList();
    }

    // Process Renewal Reminders
    @Scheduled(fixedDelay = 3600000) // Run every hour
    @Transactional
    public void sendRenewalReminders() {
        try {
            LocalDateTime renewalWindow = LocalDateTime.now().plusDays(3);
            List<Subscription> dueForRenewal = subscriptionRepository.findDueForRenewal(renewalWindow);

            for (Subscription subscription : dueForRenewal) {
                var user = userRepository.findById(subscription.getUserId()).orElse(null);
                if (user != null) {
                    emailService.sendSubscriptionRenewalReminder(user, subscription);
                }
            }
        } catch (Exception e) {
            log.error("Error sending renewal reminders", e);
        }
    }

    private SubscriptionResponse convertToResponse(Subscription subscription) {
        SubscriptionResponse response = new SubscriptionResponse();
        response.setSubscriptionId(subscription.getId());
        response.setPlanId(subscription.getPlanId());
        response.setPlanAmount(subscription.getPlanAmount());
        response.setStatus(subscription.getStatus().toString());
        response.setCurrentCycleEnd(subscription.getCurrentCycleEnd());
        response.setNextBillingDate(subscription.getNextBillingDate());
        response.setAutoRenewal(subscription.getAutoRenewal());
        response.setCycleCount(subscription.getCycleCount());
        return response;
    }
}
