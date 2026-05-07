package com.swfte.sdk.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Represents a cost-control routing rule that switches model traffic between
 * providers based on conditions (cost, latency, error-rate, region, etc.).
 */
public class RoutingRule {

    private String id;
    private String name;
    private String description;
    private String workspaceId;
    private Boolean enabled;
    private Integer priority;
    private Map<String, Object> condition;
    private List<Map<String, Object>> actions;
    private String matchModel;
    private String fallbackModel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public RoutingRule() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(String workspaceId) { this.workspaceId = workspaceId; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public Map<String, Object> getCondition() { return condition; }
    public void setCondition(Map<String, Object> condition) { this.condition = condition; }
    public List<Map<String, Object>> getActions() { return actions; }
    public void setActions(List<Map<String, Object>> actions) { this.actions = actions; }
    public String getMatchModel() { return matchModel; }
    public void setMatchModel(String matchModel) { this.matchModel = matchModel; }
    public String getFallbackModel() { return fallbackModel; }
    public void setFallbackModel(String fallbackModel) { this.fallbackModel = fallbackModel; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final RoutingRule r = new RoutingRule();
        public Builder id(String v) { r.id = v; return this; }
        public Builder name(String v) { r.name = v; return this; }
        public Builder description(String v) { r.description = v; return this; }
        public Builder enabled(Boolean v) { r.enabled = v; return this; }
        public Builder priority(Integer v) { r.priority = v; return this; }
        public Builder condition(Map<String, Object> v) { r.condition = v; return this; }
        public Builder actions(List<Map<String, Object>> v) { r.actions = v; return this; }
        public Builder matchModel(String v) { r.matchModel = v; return this; }
        public Builder fallbackModel(String v) { r.fallbackModel = v; return this; }
        public RoutingRule build() { return r; }
    }
}
