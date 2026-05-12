package com.eduplatform.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "notification.exchange";

    public static final String QUEUE_COURSE_SUBMITTED = "queue.course.submitted";
    public static final String QUEUE_COURSE_APPROVED  = "queue.course.approved";
    public static final String QUEUE_COURSE_REJECTED  = "queue.course.rejected";

    public static final String ROUTING_COURSE_SUBMITTED = "course.submitted";
    public static final String ROUTING_COURSE_APPROVED  = "course.approved";
    public static final String ROUTING_COURSE_REJECTED  = "course.rejected";

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue courseSubmittedQueue() {
        return QueueBuilder.durable(QUEUE_COURSE_SUBMITTED).build();
    }

    @Bean
    public Queue courseApprovedQueue() {
        return QueueBuilder.durable(QUEUE_COURSE_APPROVED).build();
    }

    @Bean
    public Queue courseRejectedQueue() {
        return QueueBuilder.durable(QUEUE_COURSE_REJECTED).build();
    }

    @Bean
    public Binding courseSubmittedBinding() {
        return BindingBuilder.bind(courseSubmittedQueue()).to(notificationExchange()).with(ROUTING_COURSE_SUBMITTED);
    }

    @Bean
    public Binding courseApprovedBinding() {
        return BindingBuilder.bind(courseApprovedQueue()).to(notificationExchange()).with(ROUTING_COURSE_APPROVED);
    }

    @Bean
    public Binding courseRejectedBinding() {
        return BindingBuilder.bind(courseRejectedQueue()).to(notificationExchange()).with(ROUTING_COURSE_REJECTED);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}