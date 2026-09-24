package com.example.father_retail_app.mcp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.JsonNode;

// Represents an incoming JSON-RPC 2.0 request sent by an AI client.
//
// In MCP, all client messages use this format:
// - jsonrpc: Always "2.0"
// - id: Request number or ID (used to match the response)
// - method: What the AI wants to do (e.g. "tools/list", "tools/call", "initialize")
// - params: Arguments for the method (like tool parameters)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class McpJsonRpcRequest {
    // Protocol version, defaults to "2.0"
    private String jsonrpc = "2.0";

    // Request ID sent by the client
    private Object id;

    // Action name (e.g. "initialize", "tools/list", "tools/call")
    private String method;

    // Input data/arguments for the action
    private JsonNode params;

    public McpJsonRpcRequest() {}

    public McpJsonRpcRequest(String jsonrpc, Object id, String method, JsonNode params) {
        this.jsonrpc = jsonrpc;
        this.id = id;
        this.method = method;
        this.params = params;
    }

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public JsonNode getParams() {
        return params;
    }

    public void setParams(JsonNode params) {
        this.params = params;
    }
}
