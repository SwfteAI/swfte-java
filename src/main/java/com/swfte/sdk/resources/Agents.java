package com.swfte.sdk.resources;

import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.HttpClient;
import com.swfte.sdk.models.Agent;
import com.swfte.sdk.models.AgentListResponse;
import com.swfte.sdk.models.AgentExecuteRequest;
import com.swfte.sdk.models.AgentExecuteResponse;

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
 * // Execute the agent
 * AgentExecuteResponse response = client.agents().execute(
 *     agent.getId(),
 *     AgentExecuteRequest.builder()
 *         .message("Hello!")
 *         .build()
 * );
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
        String base = client.getBaseUrl();
        if (base.contains("/gateway")) {
            base = base.replace("/v1/gateway", "");
        }
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
        
        return httpClient.postWithCustomBase(getBaseUrl(), payload, Agent.class);
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
        
        return httpClient.putWithCustomBase(getBaseUrl() + "/" + agentId, payload, Agent.class);
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
     * Execute an agent.
     *
     * @param agentId the agent ID
     * @param request the execute request
     * @return the execute response
     */
    public AgentExecuteResponse execute(String agentId, AgentExecuteRequest request) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("message", request.getMessage());
        if (request.getConversationId() != null) {
            payload.put("conversationId", request.getConversationId());
        }
        if (request.getContext() != null) {
            payload.put("context", request.getContext());
        }
        if (request.getStream() != null) {
            payload.put("stream", request.getStream());
        }
        
        return httpClient.postWithCustomBase(
            getBaseUrl() + "/" + agentId + "/execute",
            payload,
            AgentExecuteResponse.class
        );
    }
    
    /**
     * Verify an agent.
     *
     * @param agentId the agent ID
     * @return the verified agent
     */
    public Agent verify(String agentId) {
        return httpClient.postWithCustomBase(
            getBaseUrl() + "/" + agentId + "/verify",
            new HashMap<>(),
            Agent.class
        );
    }
    
    /**
     * Clone an agent.
     *
     * @param agentId the agent ID
     * @param newName the new agent name
     * @return the cloned agent
     */
    public Agent clone(String agentId, String newName) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("newName", newName);
        
        return httpClient.postWithCustomBase(
            getBaseUrl() + "/" + agentId + "/clone",
            payload,
            Agent.class
        );
    }
    
    /**
     * Toggle an agent's active status.
     *
     * @param agentId the agent ID
     * @param active the new active status
     * @return the updated agent
     */
    public Agent toggleActive(String agentId, boolean active) {
        return httpClient.postWithCustomBase(
            getBaseUrl() + "/" + agentId + "/toggle?active=" + active,
            new HashMap<>(),
            Agent.class
        );
    }
    
    /**
     * Search for agents.
     *
     * @param query the search query
     * @return list of matching agents
     */
    public List<Agent> search(String query) {
        return search(query, 0, 20);
    }
    
    /**
     * Search for agents with pagination.
     *
     * @param query the search query
     * @param page the page number
     * @param size the page size
     * @return list of matching agents
     */
    public List<Agent> search(String query, int page, int size) {
        String url = getBaseUrl() + "/search?query=" + encodeParam(query) + "&page=" + page + "&size=" + size;
        AgentListResponse response = httpClient.getWithCustomBase(url, AgentListResponse.class);
        return response.getAgentList();
    }
    
    private String encodeParam(String param) {
        try {
            return java.net.URLEncoder.encode(param, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return param;
        }
    }
}







