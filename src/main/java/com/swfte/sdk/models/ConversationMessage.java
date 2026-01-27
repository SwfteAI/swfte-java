package com.swfte.sdk.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Represents a message in a conversation.
 */
public class ConversationMessage {

    private String id;
    private String role;
    private String content;

    @JsonProperty("conversationId")
    private String conversationId;

    private String timestamp;
    private Map<String, Object> metadata;

    @JsonProperty("toolCalls")
    private List<ToolCall> toolCalls;

    @JsonProperty("toolCallId")
    private String toolCallId;

    private String name;

    // Default constructor
    public ConversationMessage() {}

    // Constructor with essential fields
    public ConversationMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public List<ToolCall> getToolCalls() {
        return toolCalls;
    }

    public void setToolCalls(List<ToolCall> toolCalls) {
        this.toolCalls = toolCalls;
    }

    public String getToolCallId() {
        return toolCallId;
    }

    public void setToolCallId(String toolCallId) {
        this.toolCallId = toolCallId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Tool call class.
     */
    public static class ToolCall {
        private String id;
        private String type;
        private Function function;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Function getFunction() {
            return function;
        }

        public void setFunction(Function function) {
            this.function = function;
        }

        public static class Function {
            private String name;
            private String arguments;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getArguments() {
                return arguments;
            }

            public void setArguments(String arguments) {
                this.arguments = arguments;
            }
        }
    }

    /**
     * Builder for ConversationMessage.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ConversationMessage message = new ConversationMessage();

        public Builder role(String role) {
            message.role = role;
            return this;
        }

        public Builder content(String content) {
            message.content = content;
            return this;
        }

        public Builder name(String name) {
            message.name = name;
            return this;
        }

        public Builder toolCalls(List<ToolCall> toolCalls) {
            message.toolCalls = toolCalls;
            return this;
        }

        public Builder toolCallId(String toolCallId) {
            message.toolCallId = toolCallId;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            message.metadata = metadata;
            return this;
        }

        public ConversationMessage build() {
            return message;
        }
    }
}
