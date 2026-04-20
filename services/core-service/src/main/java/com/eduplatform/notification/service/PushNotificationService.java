package com.eduplatform.notification.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class PushNotificationService {

    private void ensureFirebaseInitialized() {
        if (FirebaseApp.getApps().isEmpty()) {
            throw new IllegalStateException("Firebase is not configured. Set firebase.config to enable push notifications.");
        }
    }

    // Send Push Notification
    public String sendPushNotification(String deviceToken, String title,
                                       String body, Map<String, String> data) {
        try {
            ensureFirebaseInitialized();

            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message message = Message.builder()
                    .setToken(deviceToken)
                    .setNotification(notification)
                    .putAllData(data)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);

            log.info("Push notification sent: {}", response);
            return response;

        } catch (FirebaseMessagingException e) {
            log.error("Error sending push notification", e);
            throw new RuntimeException("Failed to send push notification");
        }
    }

    // Send to Multiple Devices
    public BatchResponse sendMulticast(java.util.List<String> deviceTokens,
                                       String title, String body, Map<String, String> data) {
        try {
            ensureFirebaseInitialized();

            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(deviceTokens)
                    .setNotification(notification)
                    .putAllData(data)
                    .build();

            BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);

            log.info("Multicast sent to {} devices", deviceTokens.size());
            return response;

        } catch (FirebaseMessagingException e) {
            log.error("Error sending multicast notification", e);
            throw new RuntimeException("Failed to send push notifications");
        }
    }

    // Send Topic-Based Notification
    public String sendToTopic(String topic, String title, String body, Map<String, String> data) {
        try {
            ensureFirebaseInitialized();

            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message message = Message.builder()
                    .setTopic(topic)
                    .setNotification(notification)
                    .putAllData(data)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);

            log.info("Topic notification sent to: {}", topic);
            return response;

        } catch (FirebaseMessagingException e) {
            log.error("Error sending topic notification", e);
            throw new RuntimeException("Failed to send topic notification");
        }
    }
}
