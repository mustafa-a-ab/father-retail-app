package com.example.father_retail_app.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;
import com.example.father_retail_app.entity.Order;
import com.example.father_retail_app.repository.OrderRepository;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final EmailService emailService;

    public OrderService(OrderRepository orderRepository, EmailService emailService) {
        this.orderRepository = orderRepository;
        this.emailService = emailService;
    }

    public Order saveOrder(Order order) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        order.setOrderDate(LocalDateTime.now().format(formatter));

        Order savedOrder = orderRepository.save(order);
        emailService.sendOrderEmail(savedOrder);
        return savedOrder;
    }
}
