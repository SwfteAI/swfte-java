package com.swfte.sdk.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reply from {@code POST /v1/agents/{agentId}/chat/{userId}}.
 *
 * <p>{@link #getResponse()} is the agent's reply text. Some agents-service builds
 * return it under {@code content}; both are normalised to {@code response}.
 * {@link #getRaw()} is the full response body.</p>
 */
public class AgentChatResponse {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String response;
    private final String conversationId;
    private final Map<String, Object> raw;

    public AgentChatResponse(String response, String conversationId, Map<String, Object> raw) {
        this.response = response;
        this.conversationId = conversationId;
        this.raw = raw == null ? Collections.emptyMap() : Collections.unmodifiableMap(new LinkedHashMap<>(raw));
    }

    /** Build from the raw response body. */
    public static AgentChatResponse fromMap(Map<String, Object> body) {
        Map<String, Object> data = body == null ? Collections.emptyMap() : body;
        Object reply = data.get("response");
        if (reply == null) {
            reply = data.get("content");
        }
        String text;
        if (reply == null) {
            text = "";
        } else if (reply instanceof String) {
            text = (String) reply;
        } else {
            try {
                text = MAPPER.writeValueAsString(reply);
            } catch (JsonProcessingException e) {
                text = String.valueOf(reply);
            }
        }
        Object conv = data.get("conversationId");
        return new AgentChatResponse(text, conv == null ? null : String.valueOf(conv), data);
    }

    /** The agent's reply text. */
    public String getResponse() {
        return response;
    }

    /** Pass back via {@link AgentChatOptions.Builder#conversationId(String)} to continue. */
    public String getConversationId() {
        return conversationId;
    }

    public String getSessionId() {
        return str("sessionId");
    }

    public String getRequestId() {
        return str("requestId");
    }

    public String getModel() {
        return str("model");
    }

    public String getProvider() {
        return str("provider");
    }

    /** The full response body (agentId, userId, durationMs, token counts, toolExecutions, ...). */
    public Map<String, Object> getRaw() {
        return raw;
    }

    private String str(String key) {
        Object v = raw.get(key);
        return v == null ? null : String.valueOf(v);
    }

    @Override
    public String toString() {
        return "AgentChatResponse{conversationId=" + conversationId + ", response=" + response + "}";
    }
}
