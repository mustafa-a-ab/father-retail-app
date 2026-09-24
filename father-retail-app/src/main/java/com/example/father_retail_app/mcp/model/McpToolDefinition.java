package com.example.father_retail_app.mcp.model;

import tools.jackson.databind.JsonNode;
import java.util.Map;
import java.util.function.Function;

// Defines an AI tool.
//
// Each tool has 4 parts:
// 1. name: The tool name (like "place_order")
// 2. description: Tells the AI what the tool does
// 3. inputSchema: Defines what parameters the tool needs (e.g. name, phone, items)
// 4. handler: The Java code that actually runs when the AI calls this tool
public class McpToolDefinition {
    // Tool name
    private final String name;

    // What the tool does
    private final String description;

    // Expected inputs and parameter types
    private final Map<String, Object> inputSchema;

    // Code that executes when the tool is called
    private final Function<JsonNode, Map<String, Object>> handler;

    public McpToolDefinition(String name, String description, Map<String, Object> inputSchema,
                             Function<JsonNode, Map<String, Object>> handler) {
        this.name = name;
        this.description = description;
        this.inputSchema = inputSchema;
        this.handler = handler;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Object> getInputSchema() {
        return inputSchema;
    }

    public Function<JsonNode, Map<String, Object>> getHandler() {
        return handler;
    }
}
