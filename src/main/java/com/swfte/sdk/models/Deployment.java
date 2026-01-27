package com.swfte.sdk.models;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents a model deployment.
 */
public class Deployment {
    
    public enum State {
        PENDING,
        STARTING,
        RUNNING,
        STOPPING,
        STOPPED,
        FAILED,
        TERMINATED
    }
    
    private String id;
    private String modelName;
    private String modelType;
    private State state;
    private String workspaceId;
    private String environment;
    private String podId;
    private String runpodInstanceId;
    private String endpointUrl;
    private String healthCheckUrl;
    private String statusMessage;
    private Boolean enabled;
    private Map<String, String> parameters;
    private String servingFramework;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastHealthCheck;
    
    public Deployment() {}
    
    // Getters and Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getModelName() {
        return modelName;
    }
    
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
    
    public String getModelType() {
        return modelType;
    }
    
    public void setModelType(String modelType) {
        this.modelType = modelType;
    }
    
    public State getState() {
        return state;
    }
    
    public void setState(State state) {
        this.state = state;
    }
    
    public String getWorkspaceId() {
        return workspaceId;
    }
    
    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }
    
    public String getEnvironment() {
        return environment;
    }
    
    public void setEnvironment(String environment) {
        this.environment = environment;
    }
    
    public String getPodId() {
        return podId;
    }
    
    public void setPodId(String podId) {
        this.podId = podId;
    }
    
    public String getRunpodInstanceId() {
        return runpodInstanceId;
    }
    
    public void setRunpodInstanceId(String runpodInstanceId) {
        this.runpodInstanceId = runpodInstanceId;
    }
    
    public String getEndpointUrl() {
        return endpointUrl;
    }
    
    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }
    
    public String getHealthCheckUrl() {
        return healthCheckUrl;
    }
    
    public void setHealthCheckUrl(String healthCheckUrl) {
        this.healthCheckUrl = healthCheckUrl;
    }
    
    public String getStatusMessage() {
        return statusMessage;
    }
    
    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
    
    public Boolean getEnabled() {
        return enabled;
    }
    
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    
    public Map<String, String> getParameters() {
        return parameters;
    }
    
    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
    
    public String getServingFramework() {
        return servingFramework;
    }
    
    public void setServingFramework(String servingFramework) {
        this.servingFramework = servingFramework;
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
    
    public LocalDateTime getLastHealthCheck() {
        return lastHealthCheck;
    }
    
    public void setLastHealthCheck(LocalDateTime lastHealthCheck) {
        this.lastHealthCheck = lastHealthCheck;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final Deployment deployment = new Deployment();
        
        public Builder id(String id) {
            deployment.id = id;
            return this;
        }
        
        public Builder modelName(String modelName) {
            deployment.modelName = modelName;
            return this;
        }
        
        public Builder modelType(String modelType) {
            deployment.modelType = modelType;
            return this;
        }
        
        public Builder state(State state) {
            deployment.state = state;
            return this;
        }
        
        public Builder workspaceId(String workspaceId) {
            deployment.workspaceId = workspaceId;
            return this;
        }
        
        public Builder environment(String environment) {
            deployment.environment = environment;
            return this;
        }
        
        public Builder endpointUrl(String endpointUrl) {
            deployment.endpointUrl = endpointUrl;
            return this;
        }
        
        public Builder enabled(Boolean enabled) {
            deployment.enabled = enabled;
            return this;
        }
        
        public Builder parameters(Map<String, String> parameters) {
            deployment.parameters = parameters;
            return this;
        }
        
        public Deployment build() {
            return deployment;
        }
    }
}







