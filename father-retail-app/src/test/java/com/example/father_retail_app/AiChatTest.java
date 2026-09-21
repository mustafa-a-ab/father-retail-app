package com.example.father_retail_app;

import com.example.father_retail_app.dto.chat.ChatMessage;
import com.example.father_retail_app.dto.chat.ChatResponse;
import com.example.father_retail_app.service.GrokService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {"grok.api.key="})
public class AiChatTest {

    @Autowired
    private GrokService grokService;

    @Test
    void testGrokServiceHandlesMissingKeyGracefully() {
        // When API key is blank, GrokService returns a friendly error response without throwing exception
        ChatResponse response = grokService.processChat(List.of(new ChatMessage("user", "Hello")));
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertTrue(response.getContent().contains("API Key") || response.getError().contains("API Key"));
    }
}
