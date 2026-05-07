package com.swfte.sdk.models;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents a marketplace installation in a workspace.
 */
public class Installation {

    private String id;
    private String publicationId;
    private String workspaceId;
    private String moduleId;
    private String version;
    private String status;
    private Map<String, Object> resourceMap;
    private LocalDateTime installedAt;

    public Installation() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPublicationId() { return publicationId; }
    public void setPublicationId(String publicationId) { this.publicationId = publicationId; }
    public String getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(String workspaceId) { this.workspaceId = workspaceId; }
    public String getModuleId() { return moduleId; }
    public void setModuleId(String moduleId) { this.moduleId = moduleId; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Map<String, Object> getResourceMap() { return resourceMap; }
    public void setResourceMap(Map<String, Object> resourceMap) { this.resourceMap = resourceMap; }
    public LocalDateTime getInstalledAt() { return installedAt; }
    public void setInstalledAt(LocalDateTime installedAt) { this.installedAt = installedAt; }
}
