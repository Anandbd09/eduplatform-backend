package com.eduplatform.monitoring.service;

import com.eduplatform.monitoring.model.SystemAlert;
import com.eduplatform.monitoring.repository.SystemAlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class AlertService {

    @Autowired
    private SystemAlertRepository alertRepository;

    /**
     * CREATE ALERT
     */
    public void createAlert(String alertType, String severity, String title,
                            String description, String tenantId) {
        try {
            SystemAlert alert = SystemAlert.builder()
                    .id(UUID.randomUUID().toString())
                    .alertType(alertType)
                    .severity(severity)
                    .title(title)
                    .description(description)
                    .status("ACTIVE")
                    .createdAt(LocalDateTime.now())
                    .notificationSent(false)
                    .tenantId(tenantId)
                    .build();

            alertRepository.save(alert);

            log.info("Alert created: type={}, severity={}, title={}", alertType, severity, title);

        } catch (Exception e) {
            log.error("Error creating alert", e);
        }
    }

    /**
     * SEND ALERT NOTIFICATION
     */
    public void sendNotification(String alertId, String channel) {
        try {
            // In production: implement SMS via Twilio, Email via Resend, Slack via API
            log.info("Alert notification sent: alertId={}, channel={}", alertId, channel);
        } catch (Exception e) {
            log.error("Error sending notification", e);
        }
    }
}