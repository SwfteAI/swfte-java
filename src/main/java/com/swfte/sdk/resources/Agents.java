package com.swfte.sdk.resources;

import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.HttpClient;
import com.swfte.sdk.models.Agent;
import com.swfte.sdk.models.AgentListResponse;

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
}







