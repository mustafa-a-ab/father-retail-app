package com.example.father_retail_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.father_retail_app.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

}
