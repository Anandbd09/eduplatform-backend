package com.eduplatform.payment.repository;

import com.eduplatform.payment.model.Subscription;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends MongoRepository<Subscription, String> {
    Optional<Subscription> findByRazorpaySubscriptionId(String razorpaySubscriptionId);

    Optional<Subscription> findByUserIdAndIsActiveTrue(String userId);

    List<Subscription> findByUserId(String userId);

    List<Subscription> findByStatus(String status);

    @Query("{'nextBillingDate': {$lte: ?0}, 'status': 'ACTIVE'}")
    List<Subscription> findDueForRenewal(LocalDateTime date);

    List<Subscription> findByUserIdAndStatus(String userId, String status);
}