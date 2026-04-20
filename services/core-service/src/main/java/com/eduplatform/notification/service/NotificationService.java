package com.eduplatform.notification.service;

import com.eduplatform.core.user.model.User;
import com.eduplatform.core.user.repository.UserRepository;
import com.eduplatform.notification.dto.NotificationRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class NotificationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SMSService smsService;

    @Autowired
    private PushNotificationService pushService;

    @Autowired
    private InAppNotificationService inAppService;

    // Send Multi-Channel Notification
    @Transactional
    public void sendNotification(NotificationRequest request) {
        try {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Check preferences
            // TODO: Implement preference checking

            // Send via different channels
            if (request.getSendEmail() != null && request.getSendEmail()) {
                sendViaEmail(user, request);
            }

            if (request.getSendSMS() != null && request.getSendSMS()) {
                sendViaSMS(user, request);
            }

            if (request.getSendPush() != null && request.getSendPush()) {
                sendViaPush(user, request);
            }

            if (request.getSendInApp() != null && request.getSendInApp()) {
                sendViaInApp(user, request);
            }

        } catch (Exception e) {
            log.error("Error sending notification", e);
            throw new RuntimeException("Failed to send notification");
        }
    }

    private void sendViaEmail(User user, NotificationRequest request) {
        try {
            // TODO: Implement email sending
            log.info("Sending email to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Error sending email notification", e);
        }
    }

    private void sendViaSMS(User user, NotificationRequest request) {
        try {
            if (user.getPhone() != null) {
                smsService.sendSMS(user.getPhone(), request.getMessage());
                log.info("SMS sent to: {}", user.getPhone());
            }
        } catch (Exception e) {
            log.error("Error sending SMS notification", e);
        }
    }

    private void sendViaPush(User user, NotificationRequest request) {
        try {
            List<String> deviceTokens = user.getDeviceSessions();
            if (deviceTokens == null || deviceTokens.isEmpty()) {
                log.warn("No registered device tokens for user: {}", user.getId());
                return;
            }

            Map<String, String> data = new HashMap<>();
            if (request.getChannel() != null) {
                data.put("channel", request.getChannel());
            }
            if (request.getPriority() != null) {
                data.put("priority", request.getPriority());
            }
            if (request.getActionUrl() != null) {
                data.put("actionUrl", request.getActionUrl());
            }
            if (request.getRelatedEntityId() != null) {
                data.put("relatedEntityId", request.getRelatedEntityId());
            }
            if (request.getRelatedEntityType() != null) {
                data.put("relatedEntityType", request.getRelatedEntityType());
            }

            if (deviceTokens.size() == 1) {
                pushService.sendPushNotification(
                        deviceTokens.get(0),
                        request.getTitle(),
                        request.getMessage(),
                        data
                );
            } else {
                pushService.sendMulticast(
                        deviceTokens,
                        request.getTitle(),
                        request.getMessage(),
                        data
                );
            }

            log.info("Push notification sent to user: {}", user.getId());
        } catch (Exception e) {
            log.error("Error sending push notification", e);
        }
    }

    private void sendViaInApp(User user, NotificationRequest request) {
        try {
            inAppService.createInAppNotification(
                    user.getId(),
                    request.getTitle(),
                    request.getMessage(),
                    request.getDescription(),
                    request.getActionUrl()
            );
            log.info("In-app notification created for user: {}", user.getId());
        } catch (Exception e) {
            log.error("Error creating in-app notification", e);
        }
    }
}
