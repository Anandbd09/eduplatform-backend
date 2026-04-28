// FILE 7: MessageRepository.java
package com.eduplatform.social.repository;

import com.eduplatform.social.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {

    Page<Message> findBySenderIdAndTenantId(String senderId, String tenantId, Pageable pageable);

    Page<Message> findByRecipientIdAndStatusAndTenantId(String recipientId, String status, String tenantId, Pageable pageable);

    Long countByRecipientIdAndStatusAndTenantId(String recipientId, String status, String tenantId);
}