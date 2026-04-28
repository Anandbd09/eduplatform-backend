package com.eduplatform.social.service;

import com.eduplatform.social.model.Message;
import com.eduplatform.social.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MessagingService {

    @Autowired
    private MessageRepository messageRepository;

    /**
     * ARCHIVE MESSAGE
     */
    public void archiveMessage(String messageId) {
        try {
            messageRepository.findById(messageId).ifPresent(msg -> {
                msg.setStatus("ARCHIVED");
                messageRepository.save(msg);
                log.info("Message archived: messageId={}", messageId);
            });
        } catch (Exception e) {
            log.error("Error archiving message", e);
        }
    }
}