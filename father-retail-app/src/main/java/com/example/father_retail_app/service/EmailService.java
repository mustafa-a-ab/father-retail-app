package com.example.father_retail_app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import com.example.father_retail_app.entity.Order;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String adminEmail;

    public void sendOrderEmail(Order order) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(adminEmail);
        message.setTo(adminEmail); // email from application.properties
        message.setSubject("NEW ORDER RECEIVED");
        message.setText(
                "Customer: " + order.getCustomerName() + "\n" +
                        "Phone: " + order.getCustomerPhone() + "\n" +
                        "Items: " + order.getItemsOrdered() + "\n" +
                        "Quantity: " + order.getQuantity() + "\n" +
                        "Address: " + order.getDeliveryAddress());

        try {
            mailSender.send(message);
            System.out.println("Order email successfully sent to: " + adminEmail);
        } catch (Exception e) {
            System.err.println("Failed to send order email: " + e.getMessage());
        }
    }

}
