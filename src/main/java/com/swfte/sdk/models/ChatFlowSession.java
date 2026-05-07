package com.swfte.sdk.models;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents a runtime session of a ChatFlow. A session captures one
 * conversation with one user, from greeting through completion.
 */
public class ChatFlowSession {

    private String sessionId;
    private String chatFlowId;
    private String workspaceId;
    private String channel;
    private String status;
    private String userId;
    private Map<String, Object> collectedFields;
    private Map<String, Object> metadata;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    public ChatFlowSession() {}

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getChatFlowId() { return chatFlowId; }
    public void setChatFlowId(String chatFlowId) { this.chatFlowId = chatFlowId; }
    public String getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(String workspaceId) { this.workspaceId = workspaceId; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public Map<String, Object> getCollectedFields() { return collectedFields; }
    public void setCollectedFields(Map<String, Object> collectedFields) { this.collectedFields = collectedFields; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final ChatFlowSession s = new ChatFlowSession();
        public Builder sessionId(String v) { s.sessionId = v; return this; }
        public Builder chatFlowId(String v) { s.chatFlowId = v; return this; }
        public Builder workspaceId(String v) { s.workspaceId = v; return this; }
        public Builder channel(String v) { s.channel = v; return this; }
        public Builder status(String v) { s.status = v; return this; }
        public Builder userId(String v) { s.userId = v; return this; }
        public Builder collectedFields(Map<String, Object> v) { s.collectedFields = v; return this; }
        public Builder metadata(Map<String, Object> v) { s.metadata = v; return this; }
        public ChatFlowSession build() { return s; }
    }
}
