package com.swfte.sdk.models;

import java.util.Map;

/**
 * Response from executing an agent.
 */
public class AgentExecuteResponse {
    
    private String response;
    private String conversationId;
    private Map<String, Object> metadata;
    private Usage usage;
    
    public AgentExecuteResponse() {}
    
    public String getResponse() {
        return response;
    }
    
    public void setResponse(String response) {
        this.response = response;
    }
    
    public String getConversationId() {
        return conversationId;
    }
    
    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public Usage getUsage() {
        return usage;
    }
    
    public void setUsage(Usage usage) {
        this.usage = usage;
    }
    
    public static class Usage {
        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;
        
        public Integer getPromptTokens() {
            return promptTokens;
        }
        
        public void setPromptTokens(Integer promptTokens) {
            this.promptTokens = promptTokens;
        }
        
        public Integer getCompletionTokens() {
            return completionTokens;
        }
        
        public void setCompletionTokens(Integer completionTokens) {
            this.completionTokens = completionTokens;
        }
        
        public Integer getTotalTokens() {
            return totalTokens;
        }
        
        public void setTotalTokens(Integer totalTokens) {
            this.totalTokens = totalTokens;
        }
    }
}







