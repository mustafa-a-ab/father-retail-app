package com.example.father_retail_app.service;

import com.example.father_retail_app.dto.chat.ChatMessage;
import com.example.father_retail_app.dto.chat.ChatResponse;
import com.example.father_retail_app.dto.chat.ToolCall;
import com.example.father_retail_app.entity.Order;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;

@Service
public class GrokService {

    private static final Logger logger = LoggerFactory.getLogger(GrokService.class);

    private final OrderService orderService;
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
            Your goal is to help the customer place grocery orders through conversation.
            
            Popular items available in the store:
            - Rice, Flour, Meat, Tuna, Chicken, Oil, Sugar (customers may also request other typical groceries).
            
            To place an order, you MUST collect all 5 details:
            1. Item(s) ordered (itemsOrdered)
            2. Quantity (quantity)
            3. Customer's full name (customerName)
            4. Customer's contact phone number (customerPhone)
            5. Delivery address (deliveryAddress)
            
            Guidelines:
            - Keep your responses polite, warm, and concise.
            - If any of the 5 required details are missing, politely ask the user for them.
            - When the user gives you information, acknowledge it and prompt for what is still needed.
            - Once all 5 details are known, call the 'place_order' tool immediately. Do not ask for redundant confirmations if the user already provided the info.
            - After the 'place_order' tool executes successfully, warmly confirm to the customer that their order has been placed with its details and order number.
            """;

    public GrokService(OrderService orderService, ObjectMapper objectMapper) {
        this.orderService = orderService;
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

            // 1st LLM call
            Map<String, Object> requestPayload = new HashMap<>();
            requestPayload.put("model", model);
            requestPayload.put("messages", requestMessages);
            requestPayload.put("tools", getToolsDefinition());
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
                for (JsonNode toolCall : toolCallsNode) {
                    String funcName = toolCall.path("function").path("name").asText();
                    if ("place_order".equalsIgnoreCase(funcName)) {
                        String argsString = toolCall.path("function").path("arguments").asText();
                        logger.info("Executing place_order tool with arguments: {}", argsString);

                        JsonNode args = objectMapper.readTree(argsString);
                        String customerName = args.path("customerName").asText("");
                        String customerPhone = args.path("customerPhone").asText("");
                        String itemsOrdered = args.path("itemsOrdered").asText("");
                        String quantity = args.path("quantity").asText("");
                        String deliveryAddress = args.path("deliveryAddress").asText("");

                        Order order = new Order(customerName, customerPhone, itemsOrdered, quantity, deliveryAddress);
                        Order savedOrder = orderService.saveOrder(order);

                        // Feed tool execution result back to Grok for a natural confirmation
                        Map<String, Object> assistantMessage = new HashMap<>();
                        assistantMessage.put("role", "assistant");
                        assistantMessage.put("content", content);
                        assistantMessage.put("tool_calls", objectMapper.convertValue(toolCallsNode, List.class));
                        requestMessages.add(assistantMessage);

                        Map<String, Object> toolResultMessage = new HashMap<>();
                        toolResultMessage.put("role", "tool");
                        toolResultMessage.put("tool_call_id", toolCall.path("id").asText());
                        toolResultMessage.put("content", objectMapper.writeValueAsString(Map.of(
                                "status", "SUCCESS",
                                "orderId", savedOrder.getId(),
                                "itemsOrdered", savedOrder.getItemsOrdered(),
                                "quantity", savedOrder.getQuantity(),
                                "deliveryAddress", savedOrder.getDeliveryAddress(),
                                "customerName", savedOrder.getCustomerName(),
                                "orderDate", savedOrder.getOrderDate() != null ? savedOrder.getOrderDate() : ""
                        )));
                        requestMessages.add(toolResultMessage);

                        // Call Grok again to generate the final friendly confirmation message
                        Map<String, Object> followUpPayload = new HashMap<>();
                        followUpPayload.put("model", model);
                        followUpPayload.put("messages", requestMessages);

                        JsonNode followUpResponse = callGrokApi(followUpPayload);
                        String finalMessage = followUpResponse.path("choices").get(0).path("message").path("content").asText();

                        if (finalMessage.isBlank()) {
                            finalMessage = String.format("Thank you %s! Your order #%d for %s (%s) has been placed successfully and will be delivered to %s.",
                                    savedOrder.getCustomerName(), savedOrder.getId(), savedOrder.getItemsOrdered(), savedOrder.getQuantity(), savedOrder.getDeliveryAddress());
                        }

                        return new ChatResponse(finalMessage, savedOrder);
                    }
                }
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

    private List<Map<String, Object>> getToolsDefinition() {
        Map<String, Object> customerNameProp = Map.of(
                "type", "string",
                "description", "Customer's full name");
        Map<String, Object> customerPhoneProp = Map.of(
                "type", "string",
                "description", "Customer's phone number, e.g. 0501234567");
        Map<String, Object> itemsOrderedProp = Map.of(
                "type", "string",
                "description", "Items ordered, e.g. Rice, Flour, Meat, Tuna, Chicken, Oil, Sugar");
        Map<String, Object> quantityProp = Map.of(
                "type", "string",
                "description", "Quantity of the items, e.g. 5 kg, 2 packs");
        Map<String, Object> deliveryAddressProp = Map.of(
                "type", "string",
                "description", "Full delivery address including street and building/apartment number");

        Map<String, Object> properties = Map.of(
                "customerName", customerNameProp,
                "customerPhone", customerPhoneProp,
                "itemsOrdered", itemsOrderedProp,
                "quantity", quantityProp,
                "deliveryAddress", deliveryAddressProp
        );

        Map<String, Object> parameters = Map.of(
                "type", "object",
                "properties", properties,
                "required", List.of("customerName", "customerPhone", "itemsOrdered", "quantity", "deliveryAddress")
        );

        Map<String, Object> function = Map.of(
                "name", "place_order",
                "description", "Save and place the retail grocery order when all details are collected from the customer.",
                "parameters", parameters
        );

        return List.of(Map.of(
                "type", "function",
                "function", function
        ));
    }
}
