package com.example.father_retail_app.controller;

import com.example.father_retail_app.entity.Order;
import com.example.father_retail_app.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderApiController {

    private final OrderService orderService;

    public OrderApiController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        Order saved = orderService.saveOrder(order);
        return ResponseEntity.ok(saved);
    }
}
