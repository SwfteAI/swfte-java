package com.swfte.sdk.models;

/**
 * Options for {@code Agents.chat(...)}.
 *
 * <pre>{@code
 * AgentChatOptions.builder().userId("user-42").conversationId(previous.getConversationId()).build();
 * }</pre>
 */
public class AgentChatOptions {

    /**
     * {@code userId} used when none is given. The agents service keeps one
     * conversation space per (agent, userId), so every call without a userId
     * shares this identity. Pass your own end-user id when several people talk
     * to the same agent through your application.
     */
    public static final String DEFAULT_USER_ID = "sdk-user";

    private final String userId;
    private final String conversationId;

    private AgentChatOptions(Builder builder) {
        this.userId = builder.userId;
        this.conversationId = builder.conversationId;
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Conversation owner; {@link #DEFAULT_USER_ID} when not set. */
    public String getUserId() {
        return userId == null || userId.isEmpty() ? DEFAULT_USER_ID : userId;
    }

    /** Conversation to continue, or {@code null} to start one. */
    public String getConversationId() {
        return conversationId;
    }

    public static class Builder {
        private String userId;
        private String conversationId;

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder conversationId(String conversationId) {
            this.conversationId = conversationId;
            return this;
        }

        public AgentChatOptions build() {
            return new AgentChatOptions(this);
        }
    }
}
