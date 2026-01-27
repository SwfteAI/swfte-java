package com.swfte.sdk.models;

import java.time.LocalDateTime;

/**
 * Represents an AI agent.
 */
public class Agent {
    
    private String id;
    private String agentName;
    private String description;
    private String systemPrompt;
    private String provider;
    private String model;
    private Double temperature;
    private Integer maxTokens;
    private Boolean active;
    private Boolean verified;
    private String inputType;
    private String outputType;
    private String workspaceId;
    private String mode;
    private String workflowId;
    private Boolean useWorkflow;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public Agent() {}
    
    // Getters and Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getAgentName() {
        return agentName;
    }
    
    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getSystemPrompt() {
        return systemPrompt;
    }
    
    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }
    
    public String getProvider() {
        return provider;
    }
    
    public void setProvider(String provider) {
        this.provider = provider;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public Double getTemperature() {
        return temperature;
    }
    
    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }
    
    public Integer getMaxTokens() {
        return maxTokens;
    }
    
    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    public Boolean getVerified() {
        return verified;
    }
    
    public void setVerified(Boolean verified) {
        this.verified = verified;
    }
    
    public String getInputType() {
        return inputType;
    }
    
    public void setInputType(String inputType) {
        this.inputType = inputType;
    }
    
    public String getOutputType() {
        return outputType;
    }
    
    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }
    
    public String getWorkspaceId() {
        return workspaceId;
    }
    
    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }
    
    public String getMode() {
        return mode;
    }
    
    public void setMode(String mode) {
        this.mode = mode;
    }
    
    public String getWorkflowId() {
        return workflowId;
    }
    
    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }
    
    public Boolean getUseWorkflow() {
        return useWorkflow;
    }
    
    public void setUseWorkflow(Boolean useWorkflow) {
        this.useWorkflow = useWorkflow;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * Builder for Agent.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final Agent agent = new Agent();
        
        public Builder id(String id) {
            agent.id = id;
            return this;
        }
        
        public Builder agentName(String agentName) {
            agent.agentName = agentName;
            return this;
        }
        
        public Builder description(String description) {
            agent.description = description;
            return this;
        }
        
        public Builder systemPrompt(String systemPrompt) {
            agent.systemPrompt = systemPrompt;
            return this;
        }
        
        public Builder provider(String provider) {
            agent.provider = provider;
            return this;
        }
        
        public Builder model(String model) {
            agent.model = model;
            return this;
        }
        
        public Builder temperature(Double temperature) {
            agent.temperature = temperature;
            return this;
        }
        
        public Builder maxTokens(Integer maxTokens) {
            agent.maxTokens = maxTokens;
            return this;
        }
        
        public Builder active(Boolean active) {
            agent.active = active;
            return this;
        }
        
        public Builder verified(Boolean verified) {
            agent.verified = verified;
            return this;
        }
        
        public Builder inputType(String inputType) {
            agent.inputType = inputType;
            return this;
        }
        
        public Builder outputType(String outputType) {
            agent.outputType = outputType;
            return this;
        }
        
        public Builder workspaceId(String workspaceId) {
            agent.workspaceId = workspaceId;
            return this;
        }
        
        public Builder mode(String mode) {
            agent.mode = mode;
            return this;
        }
        
        public Builder workflowId(String workflowId) {
            agent.workflowId = workflowId;
            return this;
        }
        
        public Builder useWorkflow(Boolean useWorkflow) {
            agent.useWorkflow = useWorkflow;
            return this;
        }
        
        public Agent build() {
            return agent;
        }
    }
}







