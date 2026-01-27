package com.swfte.sdk.models;

/**
 * Represents a workflow edge.
 */
public class WorkflowEdge {
    
    private String id;
    private String source;
    private String target;
    private String condition;
    
    public WorkflowEdge() {}
    
    public WorkflowEdge(String id, String source, String target) {
        this.id = id;
        this.source = source;
        this.target = target;
    }
    
    public WorkflowEdge(String id, String source, String target, String condition) {
        this.id = id;
        this.source = source;
        this.target = target;
        this.condition = condition;
    }
    
    // Getters and Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public String getTarget() {
        return target;
    }
    
    public void setTarget(String target) {
        this.target = target;
    }
    
    public String getCondition() {
        return condition;
    }
    
    public void setCondition(String condition) {
        this.condition = condition;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final WorkflowEdge edge = new WorkflowEdge();
        
        public Builder id(String id) {
            edge.id = id;
            return this;
        }
        
        public Builder source(String source) {
            edge.source = source;
            return this;
        }
        
        public Builder target(String target) {
            edge.target = target;
            return this;
        }
        
        public Builder condition(String condition) {
            edge.condition = condition;
            return this;
        }
        
        public WorkflowEdge build() {
            return edge;
        }
    }
}







