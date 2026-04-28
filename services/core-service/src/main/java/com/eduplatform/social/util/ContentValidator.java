// FILE 29: ContentValidator.java
package com.eduplatform.social.util;

import com.eduplatform.social.dto.*;
import com.eduplatform.social.exception.SocialException;

public class ContentValidator {

    /**
     * VALIDATE MESSAGE
     */
    public static void validateMessage(MessageRequest request) throws SocialException {
        if (request == null) {
            throw new SocialException("Message request cannot be null");
        }

        if (request.getRecipientId() == null || request.getRecipientId().isEmpty()) {
            throw new SocialException("Recipient ID is required");
        }

        if (request.getContent() == null || request.getContent().isEmpty()) {
            throw new SocialException("Message content is required");
        }

        if (request.getContent().length() > 5000) {
            throw new SocialException("Message content exceeds maximum length (5000 chars)");
        }
    }

    /**
     * VALIDATE FORUM THREAD
     */
    public static void validateForumThread(ForumThreadRequest request) throws SocialException {
        if (request == null) {
            throw new SocialException("Forum thread request cannot be null");
        }

        if (request.getCourseId() == null || request.getCourseId().isEmpty()) {
            throw new SocialException("Course ID is required");
        }

        if (request.getTitle() == null || request.getTitle().isEmpty()) {
            throw new SocialException("Thread title is required");
        }

        if (request.getTitle().length() > 200) {
            throw new SocialException("Title exceeds maximum length (200 chars)");
        }
    }

    /**
     * VALIDATE FORUM POST
     */
    public static void validateForumPost(ForumPostRequest request) throws SocialException {
        if (request == null) {
            throw new SocialException("Forum post request cannot be null");
        }

        if (request.getThreadId() == null || request.getThreadId().isEmpty()) {
            throw new SocialException("Thread ID is required");
        }

        if (request.getContent() == null || request.getContent().isEmpty()) {
            throw new SocialException("Post content is required");
        }

        if (request.getContent().length() > 10000) {
            throw new SocialException("Content exceeds maximum length (10000 chars)");
        }
    }
}