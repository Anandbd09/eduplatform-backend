package com.eduplatform.payment.repository;

import com.eduplatform.payment.model.Invoice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends MongoRepository<Invoice, String> {
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    List<Invoice> findByUserId(String userId);

    List<Invoice> findByPaymentId(String paymentId);

    List<Invoice> findByStatus(String status);

    List<Invoice> findByIssueDateBetween(LocalDateTime start, LocalDateTime end);
}