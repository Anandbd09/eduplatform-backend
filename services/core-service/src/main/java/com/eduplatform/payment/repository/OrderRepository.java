package com.eduplatform.payment.repository;

import com.eduplatform.payment.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    Optional<Order> findByRazorpayOrderId(String razorpayOrderId);

    List<Order> findByUserId(String userId);

    List<Order> findByUserIdAndStatus(String userId, String status);

    List<Order> findByStatusAndExpiresAtBefore(String status, LocalDateTime expiresAt);

    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    Long countByStatus(String status);
}