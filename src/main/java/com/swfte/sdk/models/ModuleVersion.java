package com.swfte.sdk.models;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents a single version of a Swfte module.
 */
public class ModuleVersion {

    private String moduleId;
    private String version;
    private String status;
    private String notes;
    private Map<String, Object> manifest;
    private Map<String, Object> impact;
    private LocalDateTime createdAt;

    public ModuleVersion() {}

    public String getModuleId() { return moduleId; }
    public void setModuleId(String moduleId) { this.moduleId = moduleId; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Map<String, Object> getManifest() { return manifest; }
    public void setManifest(Map<String, Object> manifest) { this.manifest = manifest; }
    public Map<String, Object> getImpact() { return impact; }
    public void setImpact(Map<String, Object> impact) { this.impact = impact; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final ModuleVersion v = new ModuleVersion();
        public Builder moduleId(String x) { v.moduleId = x; return this; }
        public Builder version(String x) { v.version = x; return this; }
        public Builder status(String x) { v.status = x; return this; }
        public Builder notes(String x) { v.notes = x; return this; }
        public ModuleVersion build() { return v; }
    }
}
