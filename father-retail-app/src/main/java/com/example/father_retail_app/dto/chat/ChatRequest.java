package com.example.father_retail_app.dto.chat;

import java.util.ArrayList;
import java.util.List;

public class ChatRequest {
    private List<ChatMessage> messages = new ArrayList<>();

    public ChatRequest() {
    }

    public ChatRequest(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }
}
