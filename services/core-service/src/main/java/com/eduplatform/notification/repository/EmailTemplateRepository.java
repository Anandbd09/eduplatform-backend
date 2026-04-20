package com.eduplatform.notification.repository;

import com.eduplatform.notification.model.EmailTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EmailTemplateRepository extends MongoRepository<EmailTemplate, String> {
    Optional<EmailTemplate> findByTemplateName(String templateName);

    Optional<EmailTemplate> findByIsDefaultTrue();
}