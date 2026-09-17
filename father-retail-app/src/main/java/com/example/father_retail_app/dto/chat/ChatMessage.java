package com.example.father_retail_app.dto.chat;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatMessage {
    private String role;
    private String content;
    private List<ToolCall> tool_calls;
    private String tool_call_id;

    public ChatMessage() {
    }

    public ChatMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public ChatMessage(String role, String content, String tool_call_id) {
        this.role = role;
        this.content = content;
        this.tool_call_id = tool_call_id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<ToolCall> getTool_calls() {
        return tool_calls;
    }

    public void setTool_calls(List<ToolCall> tool_calls) {
        this.tool_calls = tool_calls;
    }

    public String getTool_call_id() {
        return tool_call_id;
    }

    public void setTool_call_id(String tool_call_id) {
        this.tool_call_id = tool_call_id;
    }
}
