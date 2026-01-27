package com.swfte.sdk.models;

import java.util.Map;

/**
 * Represents a workflow node.
 */
public class WorkflowNode {
    
    private String id;
    private String type;
    private String name;
    private Position position;
    private Map<String, Object> config;
    
    public WorkflowNode() {}
    
    public WorkflowNode(String id, String type, String name, Map<String, Object> config) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.config = config;
    }
    
    // Getters and Setters
    
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
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Position getPosition() {
        return position;
    }
    
    public void setPosition(Position position) {
        this.position = position;
    }
    
    public Map<String, Object> getConfig() {
        return config;
    }
    
    public void setConfig(Map<String, Object> config) {
        this.config = config;
    }
    
    public static class Position {
        private Double x;
        private Double y;
        
        public Position() {}
        
        public Position(Double x, Double y) {
            this.x = x;
            this.y = y;
        }
        
        public Double getX() {
            return x;
        }
        
        public void setX(Double x) {
            this.x = x;
        }
        
        public Double getY() {
            return y;
        }
        
        public void setY(Double y) {
            this.y = y;
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final WorkflowNode node = new WorkflowNode();
        
        public Builder id(String id) {
            node.id = id;
            return this;
        }
        
        public Builder type(String type) {
            node.type = type;
            return this;
        }
        
        public Builder name(String name) {
            node.name = name;
            return this;
        }
        
        public Builder position(Double x, Double y) {
            node.position = new Position(x, y);
            return this;
        }
        
        public Builder config(Map<String, Object> config) {
            node.config = config;
            return this;
        }
        
        public WorkflowNode build() {
            return node;
        }
    }
}







