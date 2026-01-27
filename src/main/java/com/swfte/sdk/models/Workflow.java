package com.swfte.sdk.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Represents a workflow.
 */
public class Workflow {
    
    private String id;
    private String workflowId;
    private String name;
    private String description;
    private String workspaceId;
    private Boolean active;
    private List<WorkflowNode> nodes;
    private List<WorkflowEdge> edges;
    private Map<String, Object> variables;
    private String version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public Workflow() {}
    
    // Getters and Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getWorkflowId() {
        return workflowId;
    }
    
    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getWorkspaceId() {
        return workspaceId;
    }
    
    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    public List<WorkflowNode> getNodes() {
        return nodes;
    }
    
    public void setNodes(List<WorkflowNode> nodes) {
        this.nodes = nodes;
    }
    
    public List<WorkflowEdge> getEdges() {
        return edges;
    }
    
    public void setEdges(List<WorkflowEdge> edges) {
        this.edges = edges;
    }
    
    public Map<String, Object> getVariables() {
        return variables;
    }
    
    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
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
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final Workflow workflow = new Workflow();
        
        public Builder id(String id) {
            workflow.id = id;
            return this;
        }
        
        public Builder name(String name) {
            workflow.name = name;
            return this;
        }
        
        public Builder description(String description) {
            workflow.description = description;
            return this;
        }
        
        public Builder workspaceId(String workspaceId) {
            workflow.workspaceId = workspaceId;
            return this;
        }
        
        public Builder active(Boolean active) {
            workflow.active = active;
            return this;
        }
        
        public Builder nodes(List<WorkflowNode> nodes) {
            workflow.nodes = nodes;
            return this;
        }
        
        public Builder edges(List<WorkflowEdge> edges) {
            workflow.edges = edges;
            return this;
        }
        
        public Builder variables(Map<String, Object> variables) {
            workflow.variables = variables;
            return this;
        }
        
        public Workflow build() {
            return workflow;
        }
    }
}







