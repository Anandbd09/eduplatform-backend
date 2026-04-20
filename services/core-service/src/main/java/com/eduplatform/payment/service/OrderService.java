package com.eduplatform.payment.service;

import com.eduplatform.payment.model.Order;
import com.eduplatform.payment.model.OrderStatus;
import com.eduplatform.payment.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    // Expire pending orders after 15 minutes
    @Scheduled(fixedDelay = 300000) // Run every 5 minutes
    @Transactional
    public void expireOldOrders() {
        try {
            List<Order> expiredOrders = orderRepository.findByStatusAndExpiresAtBefore(
                    OrderStatus.PENDING.name(),
                    LocalDateTime.now()
            );

            for (Order order : expiredOrders) {
                order.setStatus(OrderStatus.EXPIRED);
                orderRepository.save(order);
                log.info("Order expired: {}", order.getId());
            }
        } catch (Exception e) {
            log.error("Error expiring orders", e);
        }
    }
}
