package com.example.father_retail_app.mcp.server;

import com.example.father_retail_app.mcp.dto.McpJsonRpcRequest;
import com.example.father_retail_app.mcp.dto.McpJsonRpcResponse;
import com.example.father_retail_app.mcp.registry.McpToolRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/mcp")
@CrossOrigin(origins = "*")
public class McpServerController {

    private static final Logger logger = LoggerFactory.getLogger(McpServerController.class);

    private final McpToolRegistry toolRegistry;
    private final ObjectMapper objectMapper;

    @Value("${mcp.server.name:father-retail-store}")
    private String serverName;

    @Value("${mcp.server.version:1.0.0}")
    private String serverVersion;

    // Active SSE sessions mapped by sessionId
    private final Map<String, SseEmitter> activeSessions = new ConcurrentHashMap<>();

    public McpServerController(McpToolRegistry toolRegistry, ObjectMapper objectMapper) {
        this.toolRegistry = toolRegistry;
        this.objectMapper = objectMapper;
    }

    /**
     * MCP SSE Transport Entrypoint: GET /mcp/sse
     * Initiates the SSE connection and sends the initial endpoint event with a sessionId.
     */
    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connectSse() {
        String sessionId = UUID.randomUUID().toString();
        // 30 minute timeout for long-lived MCP client sessions
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        activeSessions.put(sessionId, emitter);

        emitter.onCompletion(() -> {
            logger.info("MCP SSE session {} completed", sessionId);
            activeSessions.remove(sessionId);
        });
        emitter.onTimeout(() -> {
            logger.info("MCP SSE session {} timed out", sessionId);
            activeSessions.remove(sessionId);
        });
        emitter.onError(e -> {
            logger.warn("MCP SSE session {} error: {}", sessionId, e.getMessage());
            activeSessions.remove(sessionId);
        });

        // Send 'endpoint' event as specified by MCP SSE Transport Spec
        try {
            String endpointUrl = "/mcp/messages?sessionId=" + sessionId;
            emitter.send(SseEmitter.event()
                    .name("endpoint")
                    .data(endpointUrl));
            logger.info("Initialized new MCP SSE session {} -> {}", sessionId, endpointUrl);
        } catch (IOException e) {
            logger.error("Failed to emit MCP endpoint event for session {}", sessionId, e);
            emitter.completeWithError(e);
        }

        return emitter;
    }

    /**
     * MCP Messages Endpoint: POST /mcp/messages?sessionId=...
     * Handles JSON-RPC 2.0 requests, dispatches them, and emits response via SSE.
     */
    @PostMapping(value = "/messages", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> handleMessage(
            @RequestParam(name = "sessionId", required = false) String sessionId,
            @RequestBody McpJsonRpcRequest request) {

        logger.debug("Received MCP request for session {}: method={}", sessionId, request.getMethod());

        McpJsonRpcResponse response = processJsonRpc(request);

        // If this is a notification (no id), no reply is sent back per JSON-RPC spec
        if (request.getId() == null) {
            return ResponseEntity.accepted().build();
        }

        // Emit through SSE emitter if session exists
        if (sessionId != null && activeSessions.containsKey(sessionId)) {
            SseEmitter emitter = activeSessions.get(sessionId);
            try {
                String responseJson = objectMapper.writeValueAsString(response);
                emitter.send(SseEmitter.event()
                        .name("message")
                        .data(responseJson));
                return ResponseEntity.accepted().body(Map.of("status", "dispatched"));
            } catch (Exception e) {
                logger.error("Failed to send message over SSE for session {}", sessionId, e);
                return ResponseEntity.ok(response);
            }
        }

        // Fallback: return direct JSON response (convenient for curl / direct HTTP testing)
        return ResponseEntity.ok(response);
    }

    /**
     * Core JSON-RPC 2.0 Request Processor for MCP.
     */
    private McpJsonRpcResponse processJsonRpc(McpJsonRpcRequest request) {
        String method = request.getMethod() != null ? request.getMethod().trim() : "";
        Object id = request.getId();
        JsonNode params = request.getParams();

        try {
            switch (method) {
                case "initialize":
                    return handleInitialize(id, params);

                case "notifications/initialized":
                case "initialized":
                    logger.info("MCP client initialized notification received.");
                    return McpJsonRpcResponse.success(id, Map.of());

                case "ping":
                    return McpJsonRpcResponse.success(id, Map.of());

                case "tools/list":
                    return handleToolsList(id);

                case "tools/call":
                    return handleToolsCall(id, params);

                case "resources/list":
                    return handleResourcesList(id);

                case "resources/read":
                    return handleResourcesRead(id, params);

                case "prompts/list":
                    return McpJsonRpcResponse.success(id, Map.of("prompts", List.of()));

                default:
                    logger.warn("Unrecognized MCP method: {}", method);
                    return McpJsonRpcResponse.error(id, -32601, "Method not found: " + method);
            }
        } catch (Exception e) {
            logger.error("Error processing MCP method {}", method, e);
            return McpJsonRpcResponse.error(id, -32603, "Internal server error: " + e.getMessage());
        }
    }

    private McpJsonRpcResponse handleInitialize(Object id, JsonNode params) {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("tools", Map.of("listChanged", false));
        capabilities.put("resources", Map.of("subscribe", false, "listChanged", false));

        Map<String, Object> serverInfo = Map.of(
                "name", serverName,
                "version", serverVersion
        );

        Map<String, Object> result = Map.of(
                "protocolVersion", "2024-11-05",
                "capabilities", capabilities,
                "serverInfo", serverInfo
        );

        return McpJsonRpcResponse.success(id, result);
    }

    private McpJsonRpcResponse handleToolsList(Object id) {
        List<Map<String, Object>> tools = toolRegistry.toMcpToolsList();
        return McpJsonRpcResponse.success(id, Map.of("tools", tools));
    }

    private McpJsonRpcResponse handleToolsCall(Object id, JsonNode params) {
        if (params == null || !params.has("name")) {
            return McpJsonRpcResponse.error(id, -32602, "Missing 'name' in tools/call params");
        }

        String toolName = params.path("name").asText();
        JsonNode arguments = params.path("arguments");

        Map<String, Object> executionResult = toolRegistry.executeTool(toolName, arguments);

        // Format according to MCP tools/call content spec: { content: [{ type: "text", text: ... }], isError: false }
        boolean isError = "ERROR".equals(executionResult.get("status"));
        try {
            // Remove internal references before serializing
            Map<String, Object> cleanResult = new HashMap<>(executionResult);
            cleanResult.remove("_entity");

            String resultText = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(cleanResult);
            Map<String, Object> contentItem = Map.of(
                    "type", "text",
                    "text", resultText
            );

            Map<String, Object> callResult = Map.of(
                    "content", List.of(contentItem),
                    "isError", isError
            );
            return McpJsonRpcResponse.success(id, callResult);
        } catch (Exception e) {
            return McpJsonRpcResponse.error(id, -32603, "Failed to serialize tool response: " + e.getMessage());
        }
    }

    private McpJsonRpcResponse handleResourcesList(Object id) {
        List<Map<String, Object>> resources = List.of(
                Map.of(
                        "uri", "orders://recent",
                        "name", "Recent Orders",
                        "description", "Live list of the most recent orders placed in Father Retail Store",
                        "mimeType", "application/json"
                ),
                Map.of(
                        "uri", "catalog://inventory",
                        "name", "Store Grocery Inventory",
                        "description", "Complete product inventory and available unit sizes",
                        "mimeType", "application/json"
                )
        );
        return McpJsonRpcResponse.success(id, Map.of("resources", resources));
    }

    private McpJsonRpcResponse handleResourcesRead(Object id, JsonNode params) {
        if (params == null || !params.has("uri")) {
            return McpJsonRpcResponse.error(id, -32602, "Missing 'uri' in resources/read params");
        }
        String uri = params.path("uri").asText();

        try {
            if ("orders://recent".equalsIgnoreCase(uri)) {
                Map<String, Object> ordersData = toolRegistry.executeTool("list_recent_orders", objectMapper.createObjectNode());
                ordersData.remove("_entity");
                String text = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(ordersData);

                return McpJsonRpcResponse.success(id, Map.of(
                        "contents", List.of(Map.of(
                                "uri", uri,
                                "mimeType", "application/json",
                                "text", text
                        ))
                ));
            } else if ("catalog://inventory".equalsIgnoreCase(uri)) {
                Map<String, Object> inventoryData = toolRegistry.executeTool("get_store_inventory", objectMapper.createObjectNode());
                String text = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(inventoryData);

                return McpJsonRpcResponse.success(id, Map.of(
                        "contents", List.of(Map.of(
                                "uri", uri,
                                "mimeType", "application/json",
                                "text", text
                        ))
                ));
            } else {
                return McpJsonRpcResponse.error(id, -32602, "Resource not found for URI: " + uri);
            }
        } catch (Exception e) {
            return McpJsonRpcResponse.error(id, -32603, "Failed to read resource: " + e.getMessage());
        }
    }
}
