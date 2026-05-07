package com.swfte.sdk.models;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents a versioned snapshot of a ChatFlow definition.
 */
public class ChatFlowVersion {

    private String chatFlowId;
    private String version;
    private String status;
    private String notes;
    private Map<String, Object> definition;
    private LocalDateTime createdAt;
    private String createdBy;

    public ChatFlowVersion() {}

    public String getChatFlowId() { return chatFlowId; }
    public void setChatFlowId(String chatFlowId) { this.chatFlowId = chatFlowId; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Map<String, Object> getDefinition() { return definition; }
    public void setDefinition(Map<String, Object> definition) { this.definition = definition; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final ChatFlowVersion v = new ChatFlowVersion();
        public Builder chatFlowId(String x) { v.chatFlowId = x; return this; }
        public Builder version(String x) { v.version = x; return this; }
        public Builder status(String x) { v.status = x; return this; }
        public Builder notes(String x) { v.notes = x; return this; }
        public Builder definition(Map<String, Object> x) { v.definition = x; return this; }
        public ChatFlowVersion build() { return v; }
    }
}
