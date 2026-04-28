package com.eduplatform.admin.repository;

import com.eduplatform.admin.model.PaymentDispute;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentDisputeRepository extends MongoRepository<PaymentDispute, String> {
    Optional<PaymentDispute> findByPaymentId(String paymentId);

    List<PaymentDispute> findByStatusOrderByCreatedAtDesc(String status);

    List<PaymentDispute> findByUserIdAndStatus(String userId, String status);

    List<PaymentDispute> findByInstructorIdAndStatus(String instructorId, String status);
}