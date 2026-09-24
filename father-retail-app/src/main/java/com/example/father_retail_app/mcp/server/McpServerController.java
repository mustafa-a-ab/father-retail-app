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

// Controller that runs the MCP (Model Context Protocol) server.
//
// What this controller does:
// 1. Accepts connections from external AI apps (Claude Desktop, Cursor, etc.).
// 2. /mcp/sse: Opens a continuous live stream (SSE) for the AI client.
// 3. /mcp/messages: Receives JSON-RPC requests from the AI and sends back answers.
// 4. Manages the MCP protocol actions:
//    - initialize: Introduces this store to the AI.
//    - tools/list: Sends the list of grocery tools to the AI.
//    - tools/call: Executes a tool (like place_order) and returns the result.
//    - resources/list & resources/read: Lets the AI read orders and store inventory.
@RestController
@RequestMapping("/mcp")
@CrossOrigin(origins = "*")
public class McpServerController {

    private static final Logger logger = LoggerFactory.getLogger(McpServerController.class);

    // Registry of all tools (place_order, get_order, etc.)
    private final McpToolRegistry toolRegistry;

    // JSON converter for reading and writing data
    private final ObjectMapper objectMapper;

    // Server name shown to AI clients
    @Value("${mcp.server.name:father-retail-store}")
    private String serverName;

    // Server version shown to AI clients
    @Value("${mcp.server.version:1.0.0}")
    private String serverVersion;

    // Active client connections, tracked by their unique session ID
    private final Map<String, SseEmitter> activeSessions = new ConcurrentHashMap<>();

    public McpServerController(McpToolRegistry toolRegistry, ObjectMapper objectMapper) {
        this.toolRegistry = toolRegistry;
        this.objectMapper = objectMapper;
    }

    // Step 1: Client connects here via GET /mcp/sse to start a live connection.
    // We give the client a unique session ID and send an "endpoint" event telling it where to send messages.
    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connectSse() {
        String sessionId = UUID.randomUUID().toString();

        // Keep the connection open for up to 30 minutes
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        activeSessions.put(sessionId, emitter);

        // Remove the session if it finishes, times out, or runs into an error
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

        // Send the "endpoint" event so the AI client knows where to send POST messages
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

    // Step 2: Client sends requests here via POST /mcp/messages?sessionId=...
    // The request is processed, and the reply is sent back through the open SSE stream.
    @PostMapping(value = "/messages", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> handleMessage(
            @RequestParam(name = "sessionId", required = false) String sessionId,
            @RequestBody McpJsonRpcRequest request) {

        logger.debug("Received MCP request for session {}: method={}", sessionId, request.getMethod());

        McpJsonRpcResponse response = processJsonRpc(request);

        // If the request has no ID, it's just a notification (no response needed)
        if (request.getId() == null) {
            return ResponseEntity.accepted().build();
        }

        // Send the response back through the open SSE connection
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

        // Direct HTTP fallback (useful for quick curl tests)
        return ResponseEntity.ok(response);
    }

    // Routes incoming requests to the right handler based on the method name
    private McpJsonRpcResponse processJsonRpc(McpJsonRpcRequest request) {
        String method = request.getMethod() != null ? request.getMethod().trim() : "";
        Object id = request.getId();
        JsonNode params = request.getParams();

        try {
            switch (method) {
                case "initialize":
                    // Handshake: Exchange protocol version and capabilities with the AI
                    return handleInitialize(id, params);

                case "notifications/initialized":
                case "initialized":
                    // Client confirms it finished initializing
                    logger.info("MCP client initialized notification received.");
                    return McpJsonRpcResponse.success(id, Map.of());

                case "ping":
                    // Heartbeat check to see if the server is alive
                    return McpJsonRpcResponse.success(id, Map.of());

                case "tools/list":
                    // Returns the list of tools and their parameters
                    return handleToolsList(id);

                case "tools/call":
                    // Executes a tool requested by the AI
                    return handleToolsCall(id, params);

                case "resources/list":
                    // Returns the list of readable data resources
                    return handleResourcesList(id);

                case "resources/read":
                    // Reads a specific data resource
                    return handleResourcesRead(id, params);

                case "prompts/list":
                    // Prompts feature (returns empty list because none are configured)
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

    // Tells the AI the server name, version, and what features it supports
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

    // Returns all 4 store tools in MCP format
    private McpJsonRpcResponse handleToolsList(Object id) {
        List<Map<String, Object>> tools = toolRegistry.toMcpToolsList();
        return McpJsonRpcResponse.success(id, Map.of("tools", tools));
    }

    // Runs the tool requested by the AI and packages the result text
    private McpJsonRpcResponse handleToolsCall(Object id, JsonNode params) {
        if (params == null || !params.has("name")) {
            return McpJsonRpcResponse.error(id, -32602, "Missing 'name' in tools/call params");
        }

        String toolName = params.path("name").asText();
        JsonNode arguments = params.path("arguments");

        // Run the tool in McpToolRegistry
        Map<String, Object> executionResult = toolRegistry.executeTool(toolName, arguments);

        boolean isError = "ERROR".equals(executionResult.get("status"));
        try {
            // Clean internal objects before returning the JSON string to the AI
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

    // Lists available resources: recent orders and store catalog
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

    // Reads and returns data for the requested resource URI
    private McpJsonRpcResponse handleResourcesRead(Object id, JsonNode params) {
        if (params == null || !params.has("uri")) {
            return McpJsonRpcResponse.error(id, -32602, "Missing 'uri' in resources/read params");
        }
        String uri = params.path("uri").asText();

        try {
            if ("orders://recent".equalsIgnoreCase(uri)) {
                // Get recent orders and return as JSON text
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
                // Get store inventory and return as JSON text
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
