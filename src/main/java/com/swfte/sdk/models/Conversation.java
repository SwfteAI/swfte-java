package com.swfte.sdk.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Represents a conversation.
 */
public class Conversation {

    private String id;

    @JsonProperty("workspaceId")
    private String workspaceId;

    private String title;

    @JsonProperty("agentId")
    private String agentId;

    private String model;

    @JsonProperty("systemPrompt")
    private String systemPrompt;

    @JsonProperty("createdAt")
    private String createdAt;

    @JsonProperty("updatedAt")
    private String updatedAt;

    @JsonProperty("messageCount")
    private int messageCount;

    @JsonProperty("totalTokens")
    private int totalTokens;

    private Map<String, Object> metadata;

    private List<ConversationMessage> messages;

    // Default constructor
    public Conversation() {}

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    public int getTotalTokens() {
        return totalTokens;
    }

    public void setTotalTokens(int totalTokens) {
        this.totalTokens = totalTokens;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public List<ConversationMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ConversationMessage> messages) {
        this.messages = messages;
    }

    /**
     * Builder for Conversation.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Conversation conversation = new Conversation();

        public Builder title(String title) {
            conversation.title = title;
            return this;
        }

        public Builder agentId(String agentId) {
            conversation.agentId = agentId;
            return this;
        }

        public Builder model(String model) {
            conversation.model = model;
            return this;
        }

        public Builder systemPrompt(String systemPrompt) {
            conversation.systemPrompt = systemPrompt;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            conversation.metadata = metadata;
            return this;
        }

        public Conversation build() {
            return conversation;
        }
    }
}
