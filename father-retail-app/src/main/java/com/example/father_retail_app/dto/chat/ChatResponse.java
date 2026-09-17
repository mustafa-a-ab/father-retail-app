package com.example.father_retail_app.dto.chat;

import com.example.father_retail_app.entity.Order;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatResponse {
    private String role = "assistant";
    private String content;
    private Order placedOrder;
    private boolean success = true;
    private String error;

    public ChatResponse() {
    }

    public ChatResponse(String content) {
        this.content = content;
        this.success = true;
    }

    public ChatResponse(String content, Order placedOrder) {
        this.content = content;
        this.placedOrder = placedOrder;
        this.success = true;
    }

    public static ChatResponse error(String errorMessage) {
        ChatResponse resp = new ChatResponse();
        resp.setSuccess(false);
        resp.setError(errorMessage);
        resp.setContent("Sorry, I encountered an error: " + errorMessage);
        return resp;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Order getPlacedOrder() {
        return placedOrder;
    }

    public void setPlacedOrder(Order placedOrder) {
        this.placedOrder = placedOrder;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
