package com.eduplatform.payment.repository;

import com.eduplatform.payment.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {
    Optional<Payment> findByRazorpayPaymentId(String razorpayPaymentId);

    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    List<Payment> findByUserId(String userId);

    List<Payment> findByUserIdAndStatus(String userId, String status);

    List<Payment> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    Long countByStatus(String status);

    List<Payment> findByCourseId(String courseId);
}