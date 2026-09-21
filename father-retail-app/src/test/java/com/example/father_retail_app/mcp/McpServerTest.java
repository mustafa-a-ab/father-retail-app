package com.example.father_retail_app.mcp;

import com.example.father_retail_app.mcp.dto.McpJsonRpcRequest;
import com.example.father_retail_app.mcp.dto.McpJsonRpcResponse;
import com.example.father_retail_app.mcp.registry.McpToolRegistry;
import com.example.father_retail_app.mcp.server.McpServerController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class McpServerTest {

    @Autowired
    private McpToolRegistry toolRegistry;

    @Autowired
    private McpServerController mcpServerController;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testToolRegistryContainsExpectedTools() {
        List<Map<String, Object>> mcpTools = toolRegistry.toMcpToolsList();
        assertFalse(mcpTools.isEmpty());
        assertTrue(mcpTools.stream().anyMatch(t -> "place_order".equals(t.get("name"))));
        assertTrue(mcpTools.stream().anyMatch(t -> "get_order".equals(t.get("name"))));
        assertTrue(mcpTools.stream().anyMatch(t -> "list_recent_orders".equals(t.get("name"))));
        assertTrue(mcpTools.stream().anyMatch(t -> "get_store_inventory".equals(t.get("name"))));

        List<Map<String, Object>> openAiTools = toolRegistry.toOpenAiToolsDefinition();
        assertEquals(mcpTools.size(), openAiTools.size());
    }

    @Test
    void testExecuteInventoryTool() {
        Map<String, Object> result = toolRegistry.executeTool("get_store_inventory", objectMapper.createObjectNode());
        assertNotNull(result);
        assertEquals("Father Retail Store", result.get("storeName"));
        assertTrue(result.containsKey("items"));
    }

    @Test
    void testExecutePlaceOrderAndGetOrder() {
        var args = objectMapper.createObjectNode();
        args.put("customerName", "Ahmed Al-Farsi");
        args.put("customerPhone", "0559876543");
        args.put("itemsOrdered", "5kg Basmati Rice, 2L Cooking Oil");
        args.put("quantity", "2 items");
        args.put("deliveryAddress", "King Fahd Road, Apt 4B");

        Map<String, Object> placeResult = toolRegistry.executeTool("place_order", args);
        assertEquals("SUCCESS", placeResult.get("status"));
        assertNotNull(placeResult.get("orderId"));

        long orderId = ((Number) placeResult.get("orderId")).longValue();

        var getArgs = objectMapper.createObjectNode();
        getArgs.put("orderId", orderId);
        Map<String, Object> getResult = toolRegistry.executeTool("get_order", getArgs);
        assertEquals("FOUND", getResult.get("status"));
        assertEquals("Ahmed Al-Farsi", getResult.get("customerName"));
    }

    @Test
    void testMcpServerInitialize() {
        McpJsonRpcRequest request = new McpJsonRpcRequest("2.0", 1, "initialize", objectMapper.createObjectNode());
        ResponseEntity<?> entity = mcpServerController.handleMessage(null, request);
        assertNotNull(entity.getBody());
        assertTrue(entity.getBody() instanceof McpJsonRpcResponse);
        McpJsonRpcResponse response = (McpJsonRpcResponse) entity.getBody();
        assertEquals(1, response.getId());

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.getResult();
        assertEquals("2024-11-05", result.get("protocolVersion"));
        assertTrue(result.containsKey("capabilities"));
        assertTrue(result.containsKey("serverInfo"));
    }

    @Test
    void testMcpServerToolsList() {
        McpJsonRpcRequest request = new McpJsonRpcRequest("2.0", 2, "tools/list", null);
        ResponseEntity<?> entity = mcpServerController.handleMessage(null, request);
        McpJsonRpcResponse response = (McpJsonRpcResponse) entity.getBody();
        assertNotNull(response);

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.getResult();
        assertTrue(result.containsKey("tools"));
    }

    @Test
    void testMcpServerToolCall() {
        var params = objectMapper.createObjectNode();
        params.put("name", "get_store_inventory");
        params.set("arguments", objectMapper.createObjectNode());

        McpJsonRpcRequest request = new McpJsonRpcRequest("2.0", 3, "tools/call", params);
        ResponseEntity<?> entity = mcpServerController.handleMessage(null, request);
        McpJsonRpcResponse response = (McpJsonRpcResponse) entity.getBody();
        assertNotNull(response);
        assertNull(response.getError());

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.getResult();
        assertTrue(result.containsKey("content"));
        assertEquals(false, result.get("isError"));
    }

    @Test
    void testMcpServerResourcesListAndRead() {
        McpJsonRpcRequest listReq = new McpJsonRpcRequest("2.0", 4, "resources/list", null);
        ResponseEntity<?> listEntity = mcpServerController.handleMessage(null, listReq);
        McpJsonRpcResponse listResp = (McpJsonRpcResponse) listEntity.getBody();
        assertNotNull(listResp);

        var readParams = objectMapper.createObjectNode();
        readParams.put("uri", "catalog://inventory");
        McpJsonRpcRequest readReq = new McpJsonRpcRequest("2.0", 5, "resources/read", readParams);
        ResponseEntity<?> readEntity = mcpServerController.handleMessage(null, readReq);
        McpJsonRpcResponse readResp = (McpJsonRpcResponse) readEntity.getBody();
        assertNotNull(readResp);
        assertNull(readResp.getError());
    }
}
