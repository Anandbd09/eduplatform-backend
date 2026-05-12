package com.eduplatform.notification.service;

import com.eduplatform.notification.config.RabbitMQConfig;
import com.eduplatform.notification.dto.NotificationEventPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationQueueService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishCourseSubmitted(NotificationEventPayload payload) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_COURSE_SUBMITTED, payload);
            log.info("Published COURSE_SUBMITTED event for courseId={}", payload.getCourseId());
        } catch (Exception e) {
            log.error("Failed to publish COURSE_SUBMITTED event for courseId={}", payload.getCourseId(), e);
        }
    }

    public void publishCourseApproved(NotificationEventPayload payload) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_COURSE_APPROVED, payload);
            log.info("Published COURSE_APPROVED event for courseId={}", payload.getCourseId());
        } catch (Exception e) {
            log.error("Failed to publish COURSE_APPROVED event for courseId={}", payload.getCourseId(), e);
        }
    }

    public void publishCourseRejected(NotificationEventPayload payload) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_COURSE_REJECTED, payload);
            log.info("Published COURSE_REJECTED event for courseId={}", payload.getCourseId());
        } catch (Exception e) {
            log.error("Failed to publish COURSE_REJECTED event for courseId={}", payload.getCourseId(), e);
        }
    }
}
