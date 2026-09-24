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

// Service that connects the store chat to xAI Grok and runs MCP tools.
//
// How this works:
// 1. A customer sends a message in the chat.
// 2. GrokService sends the chat history to Grok along with all MCP tools.
// 3. If Grok decides to place an order or check inventory, it asks to run a tool.
// 4. GrokService executes that tool using McpToolRegistry.
// 5. GrokService sends the tool result back to Grok, which generates the final friendly reply.
@Service
public class GrokService {

    private static final Logger logger = LoggerFactory.getLogger(GrokService.class);

    // Injected tool registry containing place_order, get_order, inventory, etc.
    private final McpToolRegistry toolRegistry;

    // JSON converter
    private final ObjectMapper objectMapper;

    // HTTP client to call the xAI Grok API
    private final RestClient restClient;

    // xAI API key from application.properties
    @Value("${grok.api.key:}")
    private String apiKey;

    // xAI completions URL
    @Value("${grok.api.url:https://api.x.ai/v1/chat/completions}")
    private String apiUrl;

    // Model name (fallback:grok-2-latest)
    @Value("${grok.model:grok-2-latest}")
    private String model;

    // Instructions given to the AI so it knows how to act, what to sell, and what 5
    // details to collect
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

    // Constructor: Spring injects the tool registry and JSON converter,
    // and builds the HTTP client used to send requests to the xAI Grok API.
    public GrokService(McpToolRegistry toolRegistry, ObjectMapper objectMapper) {
        this.toolRegistry = toolRegistry;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().build();
    }

    // Handles the chat with the user and executes tools when needed
    public ChatResponse processChat(List<ChatMessage> conversationHistory) {
        // Guard against missing API key
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return ChatResponse.error(
                    "Grok API Key is missing. Please set 'grok.api.key' in application.properties or set the GROK_API_KEY environment variable.");
        }

        try {
            // Step 1: Reconstruct the full conversation context starting with the system
            // prompt
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

            // Step 2: 1st LLM call - send conversation history along with registered MCP
            // tools
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

            // Grab the first reply generated by Grok
            JsonNode firstChoice = choices.get(0);
            // Get the message object inside that reply
            JsonNode messageNode = firstChoice.path("message");
            // Extract any text Grok wrote to the user
            String content = messageNode.path("content").asText("");
            // Check if Grok wants to invoke any tools (e.g. place_order)
            JsonNode toolCallsNode = messageNode.path("tool_calls");

            // Step 3: Check if Grok decided to call one or more MCP tools
            if (toolCallsNode.isArray() && !toolCallsNode.isEmpty()) {
                Order savedOrderForReceipt = null;

                // Record the assistant's intermediate message containing the tool call request
                Map<String, Object> assistantMessage = new HashMap<>();
                assistantMessage.put("role", "assistant");
                assistantMessage.put("content", content);
                assistantMessage.put("tool_calls", objectMapper.convertValue(toolCallsNode, List.class));
                requestMessages.add(assistantMessage);

                // Step 4: Execute each requested tool in-memory via McpToolRegistry
                for (JsonNode toolCall : toolCallsNode) {
                    String toolCallId = toolCall.path("id").asText();
                    String funcName = toolCall.path("function").path("name").asText();
                    String argsString = toolCall.path("function").path("arguments").asText("{}");
                    logger.info("Executing MCP tool {} with arguments: {}", funcName, argsString);

                    JsonNode args = objectMapper.readTree(argsString);
                    Map<String, Object> executionResult = toolRegistry.executeTool(funcName, args);

                    // Extract the saved Order entity if this was an order placement (for the UI
                    // receipt card)
                    if (executionResult.get("_entity") instanceof Order o) {
                        savedOrderForReceipt = o;
                    }

                    // Remove internal objects before passing JSON back to the LLM
                    Map<String, Object> cleanResult = new HashMap<>(executionResult);
                    cleanResult.remove("_entity");

                    // Step 5: Feed the execution output back as a 'tool' role message
                    Map<String, Object> toolResultMessage = new HashMap<>();
                    toolResultMessage.put("role", "tool");
                    toolResultMessage.put("tool_call_id", toolCallId);
                    toolResultMessage.put("content", objectMapper.writeValueAsString(cleanResult));
                    requestMessages.add(toolResultMessage);
                }

                // Step 6: 2nd LLM call - ask Grok to summarize and generate a friendly customer
                // confirmation
                Map<String, Object> followUpPayload = new HashMap<>();
                followUpPayload.put("model", model);
                followUpPayload.put("messages", requestMessages);

                JsonNode followUpResponse = callGrokApi(followUpPayload);
                String finalMessage = followUpResponse.path("choices").get(0).path("message").path("content").asText();

                // Fallback: If Grok produced an empty message, format a clean receipt
                // confirmation
                if (finalMessage.isBlank() && savedOrderForReceipt != null) {
                    finalMessage = String.format(
                            "Thank you %s! Your order #%d for %s (%s) has been placed successfully and will be delivered to %s.",
                            savedOrderForReceipt.getCustomerName(), savedOrderForReceipt.getId(),
                            savedOrderForReceipt.getItemsOrdered(), savedOrderForReceipt.getQuantity(),
                            savedOrderForReceipt.getDeliveryAddress());
                }

                return new ChatResponse(finalMessage, savedOrderForReceipt);
            }

            // Standard conversational response without any tool call
            return new ChatResponse(content);

        } catch (Exception e) {
            logger.error("Error communicating with Grok API", e);
            return ChatResponse.error("Error communicating with Grok AI: " + e.getMessage());
        }
    }

    // Calls the xAI Grok API with the conversation and returns the parsed JSON
    // response
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
