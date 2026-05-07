package com.swfte.sdk.models;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents a knowledge-base dataset used by RAG-enabled agents and chatflows.
 */
public class Dataset {

    private String id;
    private String name;
    private String description;
    private String workspaceId;
    private String embeddingModel;
    private String indexingStrategy;
    private String permission;
    private Boolean apiAccessEnabled;
    private Integer documentCount;
    private Map<String, Object> retrievalConfig;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Dataset() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(String workspaceId) { this.workspaceId = workspaceId; }
    public String getEmbeddingModel() { return embeddingModel; }
    public void setEmbeddingModel(String embeddingModel) { this.embeddingModel = embeddingModel; }
    public String getIndexingStrategy() { return indexingStrategy; }
    public void setIndexingStrategy(String indexingStrategy) { this.indexingStrategy = indexingStrategy; }
    public String getPermission() { return permission; }
    public void setPermission(String permission) { this.permission = permission; }
    public Boolean getApiAccessEnabled() { return apiAccessEnabled; }
    public void setApiAccessEnabled(Boolean apiAccessEnabled) { this.apiAccessEnabled = apiAccessEnabled; }
    public Integer getDocumentCount() { return documentCount; }
    public void setDocumentCount(Integer documentCount) { this.documentCount = documentCount; }
    public Map<String, Object> getRetrievalConfig() { return retrievalConfig; }
    public void setRetrievalConfig(Map<String, Object> retrievalConfig) { this.retrievalConfig = retrievalConfig; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final Dataset d = new Dataset();
        public Builder id(String v) { d.id = v; return this; }
        public Builder name(String v) { d.name = v; return this; }
        public Builder description(String v) { d.description = v; return this; }
        public Builder workspaceId(String v) { d.workspaceId = v; return this; }
        public Builder embeddingModel(String v) { d.embeddingModel = v; return this; }
        public Builder indexingStrategy(String v) { d.indexingStrategy = v; return this; }
        public Builder permission(String v) { d.permission = v; return this; }
        public Builder apiAccessEnabled(Boolean v) { d.apiAccessEnabled = v; return this; }
        public Builder retrievalConfig(Map<String, Object> v) { d.retrievalConfig = v; return this; }
        public Dataset build() { return d; }
    }
}
