package com.swfte.sdk.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Represents a Swfte module — a reusable bundle of agents, workflows, tools,
 * datasets, and chatflows that can be versioned, published to the marketplace,
 * and installed across workspaces in one click.
 */
public class Module {

    private String id;
    private String name;
    private String description;
    private String workspaceId;
    private String status;
    private String latestVersion;
    private List<Map<String, Object>> resources;
    private Map<String, Object> manifest;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Module() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(String workspaceId) { this.workspaceId = workspaceId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getLatestVersion() { return latestVersion; }
    public void setLatestVersion(String latestVersion) { this.latestVersion = latestVersion; }
    public List<Map<String, Object>> getResources() { return resources; }
    public void setResources(List<Map<String, Object>> resources) { this.resources = resources; }
    public Map<String, Object> getManifest() { return manifest; }
    public void setManifest(Map<String, Object> manifest) { this.manifest = manifest; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final Module m = new Module();
        public Builder id(String v) { m.id = v; return this; }
        public Builder name(String v) { m.name = v; return this; }
        public Builder description(String v) { m.description = v; return this; }
        public Builder workspaceId(String v) { m.workspaceId = v; return this; }
        public Builder resources(List<Map<String, Object>> v) { m.resources = v; return this; }
        public Builder manifest(Map<String, Object> v) { m.manifest = v; return this; }
        public Module build() { return m; }
    }
}
