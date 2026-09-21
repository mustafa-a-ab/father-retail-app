package com.example.father_retail_app.service;

import com.example.father_retail_app.dto.chat.ChatMessage;
import com.example.father_retail_app.dto.chat.ChatResponse;
import com.example.father_retail_app.entity.Order;
import com.example.father_retail_app.mcp.registry.McpToolRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.*;

@Service
public class GrokService {

    private static final Logger logger = LoggerFactory.getLogger(GrokService.class);

    private final McpToolRegistry toolRegistry;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    @Value("${grok.api.key:}")
    private String apiKey;

    @Value("${grok.api.url:https://api.x.ai/v1/chat/completions}")
    private String apiUrl;

    @Value("${grok.model:grok-2-latest}")
    private String model;

    private static final String SYSTEM_PROMPT = """
            You are a friendly, efficient retail store assistant for "Father Retail Store".
            Your goal is to help customers place grocery orders and answer questions about store inventory and order status.
            
            Popular items available in the store:
            - Rice, Flour, Meat, Tuna, Chicken, Oil, Sugar, Milk, Eggs (customers may also request other typical groceries).
            
            Available capabilities / tools:
            - place_order: Place a grocery order once all 5 customer details are collected.
            - get_order: Look up order details and status using an order ID.
            - list_recent_orders: View recent store orders.
            - get_store_inventory: Check available groceries and package sizes.
            
            Order Placement Guidelines:
            - To place an order, you MUST collect all 5 details:
              1. Item(s) ordered (itemsOrdered)
              2. Quantity (quantity)
              3. Customer's full name (customerName)
              4. Contact phone number (customerPhone)
              5. Delivery address (deliveryAddress)
            - Keep your responses polite, warm, and concise.
            - If any of the 5 required details are missing, politely ask the user for them.
            - Once all 5 details are known, call the 'place_order' tool immediately. Do not ask for redundant confirmations if the user already provided the info.
            - After the tool executes successfully, warmly confirm to the customer that their order has been placed with its details and order number.
            """;

    public GrokService(McpToolRegistry toolRegistry, ObjectMapper objectMapper) {
        this.toolRegistry = toolRegistry;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().build();
    }

    public ChatResponse processChat(List<ChatMessage> conversationHistory) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return ChatResponse.error(
                    "Grok API Key is missing. Please set 'grok.api.key' in application.properties or set the GROK_API_KEY environment variable.");
        }

        try {
            // Prepare full conversation list with system prompt
            List<Map<String, Object>> requestMessages = new ArrayList<>();
            requestMessages.add(Map.of(
                    "role", "system",
                    "content", SYSTEM_PROMPT));

            for (ChatMessage msg : conversationHistory) {
                Map<String, Object> m = new HashMap<>();
                m.put("role", msg.getRole());
                m.put("content", msg.getContent() != null ? msg.getContent() : "");
                if (msg.getTool_calls() != null) {
                    m.put("tool_calls", msg.getTool_calls());
                }
                if (msg.getTool_call_id() != null) {
                    m.put("tool_call_id", msg.getTool_call_id());
                }
                requestMessages.add(m);
            }

            // 1st LLM call with dynamically registered MCP tools
            Map<String, Object> requestPayload = new HashMap<>();
            requestPayload.put("model", model);
            requestPayload.put("messages", requestMessages);
            requestPayload.put("tools", toolRegistry.toOpenAiToolsDefinition());
            requestPayload.put("tool_choice", "auto");

            JsonNode responseJson = callGrokApi(requestPayload);
            JsonNode choices = responseJson.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                return ChatResponse.error("Empty response received from Grok.");
            }

            JsonNode firstChoice = choices.get(0);
            JsonNode messageNode = firstChoice.path("message");
            String content = messageNode.path("content").asText("");
            JsonNode toolCallsNode = messageNode.path("tool_calls");

            // Check if tool call requested
            if (toolCallsNode.isArray() && !toolCallsNode.isEmpty()) {
                Order savedOrderForReceipt = null;

                // Record assistant's tool_calls message
                Map<String, Object> assistantMessage = new HashMap<>();
                assistantMessage.put("role", "assistant");
                assistantMessage.put("content", content);
                assistantMessage.put("tool_calls", objectMapper.convertValue(toolCallsNode, List.class));
                requestMessages.add(assistantMessage);

                for (JsonNode toolCall : toolCallsNode) {
                    String toolCallId = toolCall.path("id").asText();
                    String funcName = toolCall.path("function").path("name").asText();
                    String argsString = toolCall.path("function").path("arguments").asText("{}");
                    logger.info("Executing MCP tool {} with arguments: {}", funcName, argsString);

                    JsonNode args = objectMapper.readTree(argsString);
                    Map<String, Object> executionResult = toolRegistry.executeTool(funcName, args);

                    if (executionResult.get("_entity") instanceof Order o) {
                        savedOrderForReceipt = o;
                    }

                    Map<String, Object> cleanResult = new HashMap<>(executionResult);
                    cleanResult.remove("_entity");

                    // Feed tool execution result back to Grok
                    Map<String, Object> toolResultMessage = new HashMap<>();
                    toolResultMessage.put("role", "tool");
                    toolResultMessage.put("tool_call_id", toolCallId);
                    toolResultMessage.put("content", objectMapper.writeValueAsString(cleanResult));
                    requestMessages.add(toolResultMessage);
                }

                // Call Grok again to generate the final friendly confirmation message
                Map<String, Object> followUpPayload = new HashMap<>();
                followUpPayload.put("model", model);
                followUpPayload.put("messages", requestMessages);

                JsonNode followUpResponse = callGrokApi(followUpPayload);
                String finalMessage = followUpResponse.path("choices").get(0).path("message").path("content").asText();

                if (finalMessage.isBlank() && savedOrderForReceipt != null) {
                    finalMessage = String.format("Thank you %s! Your order #%d for %s (%s) has been placed successfully and will be delivered to %s.",
                            savedOrderForReceipt.getCustomerName(), savedOrderForReceipt.getId(),
                            savedOrderForReceipt.getItemsOrdered(), savedOrderForReceipt.getQuantity(),
                            savedOrderForReceipt.getDeliveryAddress());
                }

                return new ChatResponse(finalMessage, savedOrderForReceipt);
            }

            return new ChatResponse(content);

        } catch (Exception e) {
            logger.error("Error communicating with Grok API", e);
            return ChatResponse.error("Error communicating with Grok AI: " + e.getMessage());
        }
    }

    private JsonNode callGrokApi(Map<String, Object> payload) {
        String responseBody = restClient.post()
                .uri(apiUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey.trim())
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(String.class);

        try {
            return objectMapper.readTree(responseBody);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Grok API response: " + e.getMessage(), e);
        }
    }
}
