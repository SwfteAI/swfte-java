package com.swfte.sdk.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * A chat message.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Message {
    
    private String role;
    private String content;
    private String name;
    
    @JsonProperty("function_call")
    private Map<String, String> functionCall;
    
    @JsonProperty("tool_calls")
    private List<ToolCall> toolCalls;
    
    public Message() {}
    
    public Message(String role, String content) {
        this.role = role;
        this.content = content;
    }
    
    public static Message system(String content) {
        return new Message("system", content);
    }
    
    public static Message user(String content) {
        return new Message("user", content);
    }
    
    public static Message assistant(String content) {
        return new Message("assistant", content);
    }
    
    // Getters and setters
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
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Map<String, String> getFunctionCall() {
        return functionCall;
    }
    
    public void setFunctionCall(Map<String, String> functionCall) {
        this.functionCall = functionCall;
    }
    
    public List<ToolCall> getToolCalls() {
        return toolCalls;
    }
    
    public void setToolCalls(List<ToolCall> toolCalls) {
        this.toolCalls = toolCalls;
    }
    
    /**
     * Tool call structure.
     */
    public static class ToolCall {
        private String id;
        private String type;
        private ToolFunction function;
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public ToolFunction getFunction() {
            return function;
        }
        
        public void setFunction(ToolFunction function) {
            this.function = function;
        }
    }
    
    /**
     * Tool function structure.
     */
    public static class ToolFunction {
        private String name;
        private String arguments;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getArguments() {
            return arguments;
        }
        
        public void setArguments(String arguments) {
            this.arguments = arguments;
        }
    }
}

