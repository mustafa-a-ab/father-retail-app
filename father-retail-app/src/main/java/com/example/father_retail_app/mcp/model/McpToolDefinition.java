package com.example.father_retail_app.mcp.model;

import tools.jackson.databind.JsonNode;
import java.util.Map;
import java.util.function.Function;

public class McpToolDefinition {
    private final String name;
    private final String description;
    private final Map<String, Object> inputSchema;
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
