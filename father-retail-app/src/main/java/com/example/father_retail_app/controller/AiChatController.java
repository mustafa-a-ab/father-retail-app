package com.example.father_retail_app.controller;

import com.example.father_retail_app.dto.chat.ChatRequest;
import com.example.father_retail_app.dto.chat.ChatResponse;
import com.example.father_retail_app.service.GrokService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class AiChatController {

    private final GrokService grokService;

    public AiChatController(GrokService grokService) {
        this.grokService = grokService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        if (request.getMessages() == null || request.getMessages().isEmpty()) {
            return ResponseEntity.badRequest().body(ChatResponse.error("Message list cannot be empty"));
        }

        ChatResponse response = grokService.processChat(request.getMessages());
        return ResponseEntity.ok(response);
    }
}
