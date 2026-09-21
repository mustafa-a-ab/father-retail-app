package com.example.father_retail_app.mcp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class McpJsonRpcResponse {
    private String jsonrpc = "2.0";
    private Object id;
    private Object result;
    private McpError error;

    public McpJsonRpcResponse() {}

    public static McpJsonRpcResponse success(Object id, Object result) {
        McpJsonRpcResponse res = new McpJsonRpcResponse();
        res.setId(id);
        res.setResult(result);
        return res;
    }

    public static McpJsonRpcResponse error(Object id, int code, String message) {
        McpJsonRpcResponse res = new McpJsonRpcResponse();
        res.setId(id);
        res.setError(new McpError(code, message));
        return res;
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

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public McpError getError() {
        return error;
    }

    public void setError(McpError error) {
        this.error = error;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class McpError {
        private int code;
        private String message;

        public McpError() {}

        public McpError(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
