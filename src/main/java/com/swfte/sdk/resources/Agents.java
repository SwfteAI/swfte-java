package com.swfte.sdk.resources;

import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.HttpClient;
import com.swfte.sdk.models.Agent;
import com.swfte.sdk.models.AgentListResponse;
import com.swfte.sdk.models.AgentChatOptions;
import com.swfte.sdk.models.AgentChatResponse;
import com.swfte.sdk.exceptions.SwfteException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Agents API resource for managing AI agents.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * SwfteClient client = SwfteClient.builder()
 *     .apiKey("sk-swfte-...")
 *     .build();
 * 
 * // Create an agent
 * Agent agent = client.agents().create(
 *     Agent.builder()
 *         .agentName("My Assistant")
 *         .description("A helpful assistant")
 *         .provider("openai")
 *         .model("gpt-4")
 *         .systemPrompt("You are a helpful assistant.")
 *         .build()
 * );
 * 
 * // Chat with it (reply text is getResponse())
 * AgentChatResponse reply = client.agents().chat(
 *     agent.getId(), "Hello!", AgentChatOptions.builder().userId("user-42").build());
 * }</pre>
 */
public class Agents {
    
    private final HttpClient httpClient;
    private final SwfteClient client;
    
    public Agents(SwfteClient client) {
        this.client = client;
        this.httpClient = new HttpClient(client);
    }
    
    /**
     * Get the base URL for agent endpoints.
     */
    private String getBaseUrl() {
        return "/v1/agents";
    }

    private String getV2BaseUrl() {
        return "/v2/agents";
    }
    
    /**
     * Create a new agent.
     *
     * @param agent the agent to create
     * @return the created agent
     */
    public Agent create(Agent agent) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", agent.getAgentName());
        if (agent.getDescription() != null) {
            payload.put("description", agent.getDescription());
        }
        if (agent.getSystemPrompt() != null) {
            payload.put("systemPrompt", agent.getSystemPrompt());
        }
        if (agent.getProvider() != null) {
            payload.put("provider", agent.getProvider());
        }
        if (agent.getModel() != null) {
            payload.put("model", agent.getModel());
        }
        if (agent.getTemperature() != null) {
            payload.put("temperature", agent.getTemperature());
        }
        if (agent.getMaxTokens() != null) {
            payload.put("maxTokens", agent.getMaxTokens());
        }
        if (agent.getMode() != null) {
            payload.put("mode", agent.getMode());
        }
        if (client.getWorkspaceId() != null) {
            payload.put("workspaceId", client.getWorkspaceId());
        }

        return httpClient.postWithCustomBase(getV2BaseUrl(), payload, Agent.class);
    }
    
    /**
     * Get an agent by ID.
     *
     * @param agentId the agent ID
     * @return the agent
     */
    public Agent get(String agentId) {
        return httpClient.getWithCustomBase(getBaseUrl() + "/" + agentId, Agent.class);
    }
    
    /**
     * Update an existing agent.
     *
     * @param agentId the agent ID
     * @param updates the updates to apply
     * @return the updated agent
     */
    public Agent update(String agentId, Agent updates) {
        Map<String, Object> payload = new HashMap<>();
        if (updates.getAgentName() != null) {
            payload.put("name", updates.getAgentName());
        }
        if (updates.getDescription() != null) {
            payload.put("description", updates.getDescription());
        }
        if (updates.getSystemPrompt() != null) {
            payload.put("systemPrompt", updates.getSystemPrompt());
        }
        if (updates.getProvider() != null) {
            payload.put("provider", updates.getProvider());
        }
        if (updates.getModel() != null) {
            payload.put("model", updates.getModel());
        }
        if (updates.getTemperature() != null) {
            payload.put("temperature", updates.getTemperature());
        }
        if (updates.getMaxTokens() != null) {
            payload.put("maxTokens", updates.getMaxTokens());
        }
        if (updates.getActive() != null) {
            payload.put("active", updates.getActive());
        }
        
        return httpClient.patchWithCustomBase(getV2BaseUrl() + "/" + agentId, payload, Agent.class);
    }
    
    /**
     * Delete an agent.
     *
     * @param agentId the agent ID
     */
    public void delete(String agentId) {
        httpClient.deleteWithCustomBase(getBaseUrl() + "/" + agentId);
    }
    
    /**
     * List all agents.
     *
     * @return list of agents
     */
    public List<Agent> list() {
        return list(0, 20);
    }
    
    /**
     * List agents with pagination.
     *
     * @param page the page number
     * @param size the page size
     * @return list of agents
     */
    public List<Agent> list(int page, int size) {
        String url = getBaseUrl() + "?page=" + page + "&size=" + size;
        AgentListResponse response = httpClient.getWithCustomBase(url, AgentListResponse.class);
        return response.getAgentList();
    }
    
    /**
     * Toggle an agent's active status via PATCH update.
     *
     * @param agentId the agent ID
     * @param active the new active status
     * @return the updated agent
     */
    public Agent toggleActive(String agentId, boolean active) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("active", active);
        return httpClient.patchWithCustomBase(
            getV2BaseUrl() + "/" + agentId,
            payload,
            Agent.class
        );
    }

    /**
     * Associate a workflow with an agent.
     *
     * @param agentId the agent ID
     * @param workflowId the workflow ID
     * @return the updated agent
     */
    public Agent associateWorkflow(String agentId, String workflowId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("workflowId", workflowId);
        return httpClient.postWithCustomBase(
            getV2BaseUrl() + "/" + agentId + "/workflow",
            payload,
            Agent.class
        );
    }

    /**
     * Send one message to an agent as {@link AgentChatOptions#DEFAULT_USER_ID} ("sdk-user").
     *
     * @see #chat(String, String, AgentChatOptions)
     */
    public AgentChatResponse chat(String agentId, String message) {
        return chat(agentId, message, AgentChatOptions.builder().build());
    }

    /**
     * Send one message to an agent and return its reply.
     *
     * <p>{@code POST {apiBaseUrl}/v1/agents/{agentId}/chat/{userId}} with body
     * {@code {"message": ..., "conversationId": ...}}. Runs the agent's full
     * configuration (tools, knowledge, memory) and keeps history per
     * (agent, userId). Not retried: a retry would send the message twice.</p>
     *
     * @param agentId the agent to talk to
     * @param message the user's message
     * @param options userId (default "sdk-user") and conversationId; may be {@code null}
     * @return the reply; {@link AgentChatResponse#getResponse()} is the text
     * @throws com.swfte.sdk.exceptions.AuthenticationException on 401/403
     * @throws com.swfte.sdk.exceptions.RateLimitException on 429
     * @throws com.swfte.sdk.exceptions.ApiException on any other non-2xx
     */
    @SuppressWarnings("unchecked")
    public AgentChatResponse chat(String agentId, String message, AgentChatOptions options) {
        if (agentId == null || agentId.isEmpty()) {
            throw new SwfteException("agentId is required");
        }
        if (message == null || message.isEmpty()) {
            throw new SwfteException("message must be a non-empty string");
        }
        AgentChatOptions opts = options != null ? options : AgentChatOptions.builder().build();
        Map<String, Object> body = new HashMap<>();
        body.put("message", message);
        if (opts.getConversationId() != null && !opts.getConversationId().isEmpty()) {
            body.put("conversationId", opts.getConversationId());
        }
        Map<String, Object> raw = httpClient.apiRequest(
            "POST",
            getBaseUrl() + "/" + encode(agentId) + "/chat/" + encode(opts.getUserId()),
            body,
            Map.class
        );
        return AgentChatResponse.fromMap(raw);
    }

    private static String encode(String segment) {
        try {
            return URLEncoder.encode(segment, StandardCharsets.UTF_8.name()).replace("+", "%20");
        } catch (java.io.UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
