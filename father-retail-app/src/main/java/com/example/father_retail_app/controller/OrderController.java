package com.example.father_retail_app.controller;

import com.example.father_retail_app.entity.Order;
import com.example.father_retail_app.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/form")
    public String showForm(Model model) {
        model.addAttribute("order", new Order());
        return "order-form";
    }

    @PostMapping("/submit")
    public String submitOrder(@ModelAttribute Order order) {
        orderService.saveOrder(order);
        return "success";
    }

    @GetMapping({ "", "/", "/submit" })
    public String redirectToForm() {
        return "redirect:/orders/form";
    }

}
