package com.swfte.sdk.models;

import java.time.LocalDateTime;

/**
 * Represents a usage cap (per-workspace or per-model) for cost control.
 */
public class UsageCap {

    private String id;
    private String workspaceId;
    private String scope;
    private String modelId;
    private String period;
    private Double budgetUsd;
    private Double spentUsd;
    private String onExceededAction;
    private Boolean enforced;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UsageCap() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(String workspaceId) { this.workspaceId = workspaceId; }
    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }
    public String getModelId() { return modelId; }
    public void setModelId(String modelId) { this.modelId = modelId; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public Double getBudgetUsd() { return budgetUsd; }
    public void setBudgetUsd(Double budgetUsd) { this.budgetUsd = budgetUsd; }
    public Double getSpentUsd() { return spentUsd; }
    public void setSpentUsd(Double spentUsd) { this.spentUsd = spentUsd; }
    public String getOnExceededAction() { return onExceededAction; }
    public void setOnExceededAction(String onExceededAction) { this.onExceededAction = onExceededAction; }
    public Boolean getEnforced() { return enforced; }
    public void setEnforced(Boolean enforced) { this.enforced = enforced; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final UsageCap c = new UsageCap();
        public Builder id(String v) { c.id = v; return this; }
        public Builder scope(String v) { c.scope = v; return this; }
        public Builder modelId(String v) { c.modelId = v; return this; }
        public Builder period(String v) { c.period = v; return this; }
        public Builder budgetUsd(Double v) { c.budgetUsd = v; return this; }
        public Builder onExceededAction(String v) { c.onExceededAction = v; return this; }
        public Builder enforced(Boolean v) { c.enforced = v; return this; }
        public UsageCap build() { return c; }
    }
}
