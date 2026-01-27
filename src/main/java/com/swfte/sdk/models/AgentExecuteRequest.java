package com.swfte.sdk.models;

import java.util.Map;

/**
 * Request for executing an agent.
 */
public class AgentExecuteRequest {
    
    private String message;
    private String conversationId;
    private Map<String, Object> context;
    private Boolean stream;
    
    public AgentExecuteRequest() {}
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getConversationId() {
        return conversationId;
    }
    
    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
    
    public Map<String, Object> getContext() {
        return context;
    }
    
    public void setContext(Map<String, Object> context) {
        this.context = context;
    }
    
    public Boolean getStream() {
        return stream;
    }
    
    public void setStream(Boolean stream) {
        this.stream = stream;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final AgentExecuteRequest request = new AgentExecuteRequest();
        
        public Builder message(String message) {
            request.message = message;
            return this;
        }
        
        public Builder conversationId(String conversationId) {
            request.conversationId = conversationId;
            return this;
        }
        
        public Builder context(Map<String, Object> context) {
            request.context = context;
            return this;
        }
        
        public Builder stream(Boolean stream) {
            request.stream = stream;
            return this;
        }
        
        public AgentExecuteRequest build() {
            return request;
        }
    }
}







