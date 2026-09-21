package com.example.father_retail_app.mcp.registry;

import com.example.father_retail_app.entity.Order;
import com.example.father_retail_app.mcp.model.McpToolDefinition;
import com.example.father_retail_app.repository.OrderRepository;
import com.example.father_retail_app.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class McpToolRegistry {

    private static final Logger logger = LoggerFactory.getLogger(McpToolRegistry.class);

    private final Map<String, McpToolDefinition> tools = new ConcurrentHashMap<>();
    private final OrderService orderService;
    private final OrderRepository orderRepository;

    public McpToolRegistry(OrderService orderService, OrderRepository orderRepository) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        registerDefaultTools();
    }

    private void registerDefaultTools() {
        // 1. place_order
        Map<String, Object> placeOrderProps = Map.of(
                "customerName", Map.of("type", "string", "description", "Customer's full name"),
                "customerPhone", Map.of("type", "string", "description", "Customer's contact phone number, e.g. 0501234567"),
                "itemsOrdered", Map.of("type", "string", "description", "Items ordered, e.g. Rice, Flour, Meat, Tuna, Chicken, Oil, Sugar"),
                "quantity", Map.of("type", "string", "description", "Quantity of the items, e.g. 5 kg, 2 packs"),
                "deliveryAddress", Map.of("type", "string", "description", "Full delivery address including street and apartment number")
        );
        Map<String, Object> placeOrderSchema = Map.of(
                "type", "object",
                "properties", placeOrderProps,
                "required", List.of("customerName", "customerPhone", "itemsOrdered", "quantity", "deliveryAddress")
        );

        registerTool(new McpToolDefinition(
                "place_order",
                "Save and place the retail grocery order when all details are collected from the customer.",
                placeOrderSchema,
                args -> {
                    String customerName = args.path("customerName").asText("");
                    String customerPhone = args.path("customerPhone").asText("");
                    String itemsOrdered = args.path("itemsOrdered").asText("");
                    String quantity = args.path("quantity").asText("");
                    String deliveryAddress = args.path("deliveryAddress").asText("");

                    Order order = new Order(customerName, customerPhone, itemsOrdered, quantity, deliveryAddress);
                    Order savedOrder = orderService.saveOrder(order);

                    Map<String, Object> result = new HashMap<>();
                    result.put("status", "SUCCESS");
                    result.put("orderId", savedOrder.getId());
                    result.put("itemsOrdered", savedOrder.getItemsOrdered());
                    result.put("quantity", savedOrder.getQuantity());
                    result.put("deliveryAddress", savedOrder.getDeliveryAddress());
                    result.put("customerName", savedOrder.getCustomerName());
                    result.put("customerPhone", savedOrder.getCustomerPhone());
                    result.put("orderDate", savedOrder.getOrderDate() != null ? savedOrder.getOrderDate() : "");
                    result.put("_entity", savedOrder);
                    return result;
                }
        ));

        // 2. get_order
        Map<String, Object> getOrderProps = Map.of(
                "orderId", Map.of("type", "integer", "description", "The unique numeric ID of the order")
        );
        Map<String, Object> getOrderSchema = Map.of(
                "type", "object",
                "properties", getOrderProps,
                "required", List.of("orderId")
        );

        registerTool(new McpToolDefinition(
                "get_order",
                "Retrieve order details and status by order ID.",
                getOrderSchema,
                args -> {
                    long orderId = args.path("orderId").asLong(0);
                    Optional<Order> opt = orderRepository.findById(orderId);
                    if (opt.isPresent()) {
                        Order o = opt.get();
                        Map<String, Object> result = new HashMap<>();
                        result.put("status", "FOUND");
                        result.put("orderId", o.getId());
                        result.put("customerName", o.getCustomerName());
                        result.put("customerPhone", o.getCustomerPhone());
                        result.put("itemsOrdered", o.getItemsOrdered());
                        result.put("quantity", o.getQuantity());
                        result.put("deliveryAddress", o.getDeliveryAddress());
                        result.put("orderDate", o.getOrderDate());
                        return result;
                    } else {
                        return Map.of("status", "NOT_FOUND", "message", "Order #" + orderId + " was not found.");
                    }
                }
        ));

        // 3. list_recent_orders
        Map<String, Object> listOrdersProps = Map.of(
                "limit", Map.of("type", "integer", "description", "Maximum number of recent orders to retrieve (default 10)")
        );
        Map<String, Object> listOrdersSchema = Map.of(
                "type", "object",
                "properties", listOrdersProps
        );

        registerTool(new McpToolDefinition(
                "list_recent_orders",
                "List the most recently placed grocery orders in the retail store.",
                listOrdersSchema,
                args -> {
                    int limit = args.path("limit").asInt(10);
                    if (limit <= 0) limit = 10;
                    List<Order> all = orderRepository.findAll();
                    // Take the last 'limit' items reversed
                    List<Map<String, Object>> summary = new ArrayList<>();
                    int start = Math.max(0, all.size() - limit);
                    for (int i = all.size() - 1; i >= start; i--) {
                        Order o = all.get(i);
                        summary.add(Map.of(
                                "orderId", o.getId(),
                                "customerName", o.getCustomerName(),
                                "itemsOrdered", o.getItemsOrdered(),
                                "quantity", o.getQuantity(),
                                "deliveryAddress", o.getDeliveryAddress(),
                                "orderDate", o.getOrderDate() != null ? o.getOrderDate() : ""
                        ));
                    }
                    return Map.of("totalOrders", all.size(), "orders", summary);
                }
        ));

        // 4. get_store_inventory
        Map<String, Object> inventoryProps = Map.of(
                "category", Map.of("type", "string", "description", "Optional product category filter, e.g. staples, meat, dairy, pantry")
        );
        Map<String, Object> inventorySchema = Map.of(
                "type", "object",
                "properties", inventoryProps
        );

        registerTool(new McpToolDefinition(
                "get_store_inventory",
                "Retrieve the available grocery products, available package sizes, and standard units in the store.",
                inventorySchema,
                args -> {
                    List<Map<String, String>> catalog = List.of(
                            Map.of("item", "Basmati & White Rice", "category", "staples", "availableUnits", "1kg, 5kg, 10kg, 20kg bags", "inStock", "yes"),
                            Map.of("item", "Wheat & All-Purpose Flour", "category", "staples", "availableUnits", "1kg, 2kg, 5kg bags", "inStock", "yes"),
                            Map.of("item", "Fresh Beef & Mutton", "category", "meat", "availableUnits", "per kg (1kg, 2kg, etc.)", "inStock", "yes"),
                            Map.of("item", "Fresh & Frozen Chicken", "category", "meat", "availableUnits", "Whole (900g, 1100g) or cuts per kg", "inStock", "yes"),
                            Map.of("item", "Canned Tuna", "category", "pantry", "availableUnits", "185g cans / 3-pack bundle", "inStock", "yes"),
                            Map.of("item", "Pure Corn & Sunflower Oil", "category", "pantry", "availableUnits", "1L, 1.8L, 5L bottles", "inStock", "yes"),
                            Map.of("item", "White Refined Sugar", "category", "staples", "availableUnits", "1kg, 2kg, 5kg bags", "inStock", "yes"),
                            Map.of("item", "Fresh Whole Milk & Dairy", "category", "dairy", "availableUnits", "1L, 2L cartons", "inStock", "yes"),
                            Map.of("item", "Farm Eggs", "category", "dairy", "availableUnits", "Carton of 30 eggs / 12 eggs", "inStock", "yes")
                    );
                    return Map.of("storeName", "Father Retail Store", "items", catalog);
                }
        ));

        logger.info("Registered {} default MCP tools.", tools.size());
    }

    public void registerTool(McpToolDefinition tool) {
        tools.put(tool.getName(), tool);
    }

    public Optional<McpToolDefinition> getTool(String name) {
        return Optional.ofNullable(tools.get(name));
    }

    public Collection<McpToolDefinition> getAllTools() {
        return tools.values();
    }

    /**
     * Converts registered tools to MCP Specification format for tools/list.
     */
    public List<Map<String, Object>> toMcpToolsList() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (McpToolDefinition tool : tools.values()) {
            list.add(Map.of(
                    "name", tool.getName(),
                    "description", tool.getDescription(),
                    "inputSchema", tool.getInputSchema()
            ));
        }
        return list;
    }

    /**
     * Converts registered tools to OpenAI/Grok function calling format.
     */
    public List<Map<String, Object>> toOpenAiToolsDefinition() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (McpToolDefinition tool : tools.values()) {
            Map<String, Object> function = Map.of(
                    "name", tool.getName(),
                    "description", tool.getDescription(),
                    "parameters", tool.getInputSchema()
            );
            list.add(Map.of(
                    "type", "function",
                    "function", function
            ));
        }
        return list;
    }

    /**
     * Executes the requested tool dynamically.
     */
    public Map<String, Object> executeTool(String toolName, JsonNode args) {
        McpToolDefinition tool = tools.get(toolName);
        if (tool == null) {
            return Map.of(
                    "status", "ERROR",
                    "error", "Unknown tool: " + toolName
            );
        }
        try {
            return tool.getHandler().apply(args);
        } catch (Exception e) {
            logger.error("Error executing tool {}", toolName, e);
            return Map.of(
                    "status", "ERROR",
                    "error", "Failed to execute " + toolName + ": " + e.getMessage()
            );
        }
    }
}
