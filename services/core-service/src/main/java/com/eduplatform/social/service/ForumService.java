package com.eduplatform.social.service;

import com.eduplatform.social.model.ForumThread;
import com.eduplatform.social.repository.ForumThreadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ForumService {

    @Autowired
    private ForumThreadRepository threadRepository;

    /**
     * PIN THREAD
     */
    public void pinThread(String threadId) {
        try {
            threadRepository.findById(threadId).ifPresent(thread -> {
                thread.setIsPinned(true);
                thread.setStatus("PINNED");
                threadRepository.save(thread);
                log.info("Thread pinned: threadId={}", threadId);
            });
        } catch (Exception e) {
            log.error("Error pinning thread", e);
        }
    }

    /**
     * LOCK THREAD
     */
    public void lockThread(String threadId) {
        try {
            threadRepository.findById(threadId).ifPresent(thread -> {
                thread.setIsLocked(true);
                thread.setStatus("LOCKED");
                threadRepository.save(thread);
                log.info("Thread locked: threadId={}", threadId);
            });
        } catch (Exception e) {
            log.error("Error locking thread", e);
        }
    }
}