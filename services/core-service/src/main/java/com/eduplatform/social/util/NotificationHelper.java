// FILE 27: NotificationHelper.java
package com.eduplatform.social.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NotificationHelper {

    /**
     * NOTIFY USER FOLLOWED
     */
    public static void notifyUserFollowed(String userId, String followerId, String tenantId) {
        try {
            // In production: send via Resend/Twilio/Firebase
            log.info("Notification: User {} was followed by {}", userId, followerId);
        } catch (Exception e) {
            log.warn("Error sending follow notification", e);
        }
    }

    /**
     * NOTIFY NEW MESSAGE
     */
    public static void notifyNewMessage(String recipientId, String senderId, String tenantId) {
        try {
            log.info("Notification: User {} received message from {}", recipientId, senderId);
        } catch (Exception e) {
            log.warn("Error sending message notification", e);
        }
    }

    /**
     * NOTIFY MENTION
     */
    public static void notifyMention(String mentionedUserId, String authorId, String tenantId) {
        try {
            log.info("Notification: User {} was mentioned by {}", mentionedUserId, authorId);
        } catch (Exception e) {
            log.warn("Error sending mention notification", e);
        }
    }
}