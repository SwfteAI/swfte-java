package com.swfte.sdk.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Represents a Swfte ChatFlow — a conversational form definition (onboarding,
 * lead-qualification, support, surveys) that can be deployed across web,
 * WhatsApp, Telegram, and voice channels.
 */
public class ChatFlow {

    private String id;
    private String name;
    private String description;
    private String workspaceId;
    private String status;
    private Boolean deployed;
    private List<Map<String, Object>> fields;
    private List<Map<String, Object>> actions;
    private Map<String, Object> definition;
    private Map<String, Object> voice;
    private String greeting;
    private String version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ChatFlow() {}

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
    public Boolean getDeployed() { return deployed; }
    public void setDeployed(Boolean deployed) { this.deployed = deployed; }
    public List<Map<String, Object>> getFields() { return fields; }
    public void setFields(List<Map<String, Object>> fields) { this.fields = fields; }
    public List<Map<String, Object>> getActions() { return actions; }
    public void setActions(List<Map<String, Object>> actions) { this.actions = actions; }
    public Map<String, Object> getDefinition() { return definition; }
    public void setDefinition(Map<String, Object> definition) { this.definition = definition; }
    public Map<String, Object> getVoice() { return voice; }
    public void setVoice(Map<String, Object> voice) { this.voice = voice; }
    public String getGreeting() { return greeting; }
    public void setGreeting(String greeting) { this.greeting = greeting; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final ChatFlow flow = new ChatFlow();
        public Builder id(String v) { flow.id = v; return this; }
        public Builder name(String v) { flow.name = v; return this; }
        public Builder description(String v) { flow.description = v; return this; }
        public Builder workspaceId(String v) { flow.workspaceId = v; return this; }
        public Builder status(String v) { flow.status = v; return this; }
        public Builder deployed(Boolean v) { flow.deployed = v; return this; }
        public Builder fields(List<Map<String, Object>> v) { flow.fields = v; return this; }
        public Builder actions(List<Map<String, Object>> v) { flow.actions = v; return this; }
        public Builder definition(Map<String, Object> v) { flow.definition = v; return this; }
        public Builder voice(Map<String, Object> v) { flow.voice = v; return this; }
        public Builder greeting(String v) { flow.greeting = v; return this; }
        public Builder version(String v) { flow.version = v; return this; }
        public ChatFlow build() { return flow; }
    }
}
