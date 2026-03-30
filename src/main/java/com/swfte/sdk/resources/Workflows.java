package com.swfte.sdk.resources;

import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.HttpClient;
import com.swfte.sdk.models.Workflow;
import com.swfte.sdk.models.WorkflowNode;
import com.swfte.sdk.models.WorkflowEdge;
import com.swfte.sdk.models.WorkflowExecution;
import com.swfte.sdk.models.WorkflowListResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Workflows API resource for managing workflows.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * SwfteClient client = SwfteClient.builder()
 *     .apiKey("sk-swfte-...")
 *     .build();
 * 
 * // Create a workflow
 * List<WorkflowNode> nodes = Arrays.asList(
 *     WorkflowNode.builder().id("start").type("TRIGGER").build(),
 *     WorkflowNode.builder().id("llm").type("LLM").build(),
 *     WorkflowNode.builder().id("end").type("END").build()
 * );
 * 
 * List<WorkflowEdge> edges = Arrays.asList(
 *     new WorkflowEdge("e1", "start", "llm"),
 *     new WorkflowEdge("e2", "llm", "end")
 * );
 * 
 * Workflow workflow = client.workflows().create(
 *     Workflow.builder()
 *         .name("My Workflow")
 *         .nodes(nodes)
 *         .edges(edges)
 *         .build()
 * );
 * 
 * // Execute the workflow
 * Map<String, Object> inputs = new HashMap<>();
 * inputs.put("message", "Hello!");
 * WorkflowExecution execution = client.workflows().execute(workflow.getId(), inputs);
 * 
 * // Wait for completion
 * WorkflowExecution result = client.workflows().waitForCompletion(execution.getId());
 * }</pre>
 */
public class Workflows {
    
    private final HttpClient httpClient;
    private final SwfteClient client;
    
    public Workflows(SwfteClient client) {
        this.client = client;
        this.httpClient = new HttpClient(client);
    }
    
    /**
     * Get the base URL for workflow endpoints.
     */
    private String getBaseUrl() {
        return "/v2/workflows";
    }
    
    /**
     * Create a new workflow.
     *
     * @param workflow the workflow to create
     * @return the created workflow
     */
    public Workflow create(Workflow workflow) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", workflow.getName());
        
        if (workflow.getDescription() != null) {
            payload.put("description", workflow.getDescription());
        }
        if (workflow.getNodes() != null) {
            payload.put("nodes", convertNodes(workflow.getNodes()));
        }
        if (workflow.getEdges() != null) {
            payload.put("edges", convertEdges(workflow.getEdges()));
        }
        if (workflow.getVariables() != null) {
            payload.put("variables", workflow.getVariables());
        }
        payload.put("active", workflow.getActive() != null ? workflow.getActive() : true);
        if (client.getWorkspaceId() != null) {
            payload.put("workspaceId", client.getWorkspaceId());
        }
        
        return httpClient.postWithCustomBase(getBaseUrl(), payload, Workflow.class);
    }
    
    /**
     * Get a workflow by ID.
     *
     * @param workflowId the workflow ID
     * @return the workflow
     */
    public Workflow get(String workflowId) {
        return httpClient.getWithCustomBase(getBaseUrl() + "/" + workflowId, Workflow.class);
    }
    
    /**
     * Update an existing workflow.
     *
     * @param workflowId the workflow ID
     * @param updates the updates to apply
     * @return the updated workflow
     */
    public Workflow update(String workflowId, Workflow updates) {
        Workflow current = get(workflowId);
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", updates.getName() != null ? updates.getName() : current.getName());
        payload.put("description", updates.getDescription() != null ? updates.getDescription() : current.getDescription());
        payload.put("nodes", updates.getNodes() != null ? convertNodes(updates.getNodes()) : convertNodes(current.getNodes()));
        payload.put("edges", updates.getEdges() != null ? convertEdges(updates.getEdges()) : convertEdges(current.getEdges()));
        payload.put("active", updates.getActive() != null ? updates.getActive() : current.getActive());
        payload.put("variables", updates.getVariables() != null ? updates.getVariables() : current.getVariables());
        
        return httpClient.putWithCustomBase(getBaseUrl() + "/" + workflowId, payload, Workflow.class);
    }
    
    /**
     * Delete a workflow.
     *
     * @param workflowId the workflow ID
     */
    public void delete(String workflowId) {
        delete(workflowId, false);
    }
    
    /**
     * Delete a workflow with force option.
     *
     * @param workflowId the workflow ID
     * @param force force deletion
     */
    public void delete(String workflowId, boolean force) {
        String url = force ? getBaseUrl() + "/" + workflowId + "?force=true" : getBaseUrl() + "/" + workflowId;
        httpClient.deleteWithCustomBase(url);
    }
    
    /**
     * List all workflows.
     *
     * @return list of workflows
     */
    public List<Workflow> list() {
        return list(0, 20);
    }
    
    /**
     * List workflows with pagination.
     *
     * @param page the page number
     * @param size the page size
     * @return list of workflows
     */
    public List<Workflow> list(int page, int size) {
        String url = getBaseUrl() + "?page=" + page + "&size=" + size;
        WorkflowListResponse response = httpClient.getWithCustomBase(url, WorkflowListResponse.class);
        return response.getWorkflowList();
    }
    
    /**
     * Validate a workflow definition.
     *
     * @param workflow the workflow to validate
     * @return validation result as a map
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> validate(Workflow workflow) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", workflow.getName());
        if (workflow.getNodes() != null) {
            payload.put("nodes", convertNodes(workflow.getNodes()));
        }
        if (workflow.getEdges() != null) {
            payload.put("edges", convertEdges(workflow.getEdges()));
        }
        
        return httpClient.postWithCustomBase(getBaseUrl() + "/validate", payload, Map.class);
    }
    
    /**
     * Execute a workflow.
     *
     * @param workflowId the workflow ID
     * @param inputs the workflow inputs
     * @return the workflow execution
     */
    public WorkflowExecution execute(String workflowId, Map<String, Object> inputs) {
        return execute(workflowId, inputs, false);
    }
    
    /**
     * Execute a workflow with options.
     *
     * @param workflowId the workflow ID
     * @param inputs the workflow inputs
     * @param skipValidation whether to skip validation
     * @return the workflow execution
     */
    public WorkflowExecution execute(String workflowId, Map<String, Object> inputs, boolean skipValidation) {
        String url = skipValidation 
            ? getBaseUrl() + "/" + workflowId + "/execute?skipValidation=true"
            : getBaseUrl() + "/" + workflowId + "/execute";
        
        return httpClient.postWithCustomBase(url, inputs != null ? inputs : new HashMap<>(), WorkflowExecution.class);
    }
    
    /**
     * Get execution status.
     *
     * @param executionId the execution ID
     * @return the workflow execution
     */
    public WorkflowExecution getExecutionStatus(String executionId) {
        return httpClient.getWithCustomBase(
            getBaseUrl() + "/executions/" + executionId + "/status",
            WorkflowExecution.class
        );
    }
    
    /**
     * Pause a running execution.
     *
     * @param executionId the execution ID
     * @return the paused execution
     */
    public WorkflowExecution pauseExecution(String executionId) {
        return httpClient.postWithCustomBase(
            getBaseUrl() + "/executions/" + executionId + "/pause",
            new HashMap<>(),
            WorkflowExecution.class
        );
    }
    
    /**
     * Resume a paused execution.
     *
     * @param executionId the execution ID
     * @return the resumed execution
     */
    public WorkflowExecution resumeExecution(String executionId) {
        return httpClient.postWithCustomBase(
            getBaseUrl() + "/executions/" + executionId + "/resume",
            new HashMap<>(),
            WorkflowExecution.class
        );
    }
    
    /**
     * Get execution history for a workflow.
     *
     * @param workflowId the workflow ID
     * @return list of executions
     */
    @SuppressWarnings("unchecked")
    public List<WorkflowExecution> getExecutionHistory(String workflowId) {
        Object response = httpClient.getWithCustomBase(
            getBaseUrl() + "/" + workflowId + "/executions",
            Object.class
        );
        if (response instanceof List) {
            return (List<WorkflowExecution>) response;
        }
        return new ArrayList<>();
    }
    
    /**
     * Wait for a workflow execution to complete.
     *
     * @param executionId the execution ID
     * @return the completed execution
     * @throws RuntimeException if timeout or execution fails
     */
    public WorkflowExecution waitForCompletion(String executionId) {
        return waitForCompletion(executionId, 300000, 5000);
    }
    
    /**
     * Wait for a workflow execution to complete with custom timeout.
     *
     * @param executionId the execution ID
     * @param timeoutMs timeout in milliseconds
     * @param pollIntervalMs poll interval in milliseconds
     * @return the completed execution
     * @throws RuntimeException if timeout or execution fails
     */
    public WorkflowExecution waitForCompletion(String executionId, long timeoutMs, long pollIntervalMs) {
        long startTime = System.currentTimeMillis();
        
        while (true) {
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed > timeoutMs) {
                throw new RuntimeException("Execution " + executionId + " did not complete within " + timeoutMs + "ms");
            }
            
            WorkflowExecution execution = getExecutionStatus(executionId);
            
            if (execution.getStatus() == WorkflowExecution.Status.COMPLETED) {
                return execution;
            } else if (execution.getStatus() == WorkflowExecution.Status.FAILED) {
                throw new RuntimeException("Execution " + executionId + " failed: " + execution.getError());
            } else if (execution.getStatus() == WorkflowExecution.Status.CANCELLED) {
                throw new RuntimeException("Execution " + executionId + " was cancelled");
            }
            
            try {
                Thread.sleep(pollIntervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for execution", e);
            }
        }
    }
    
    /**
     * Clone a workflow.
     *
     * @param workflowId the workflow ID
     * @param newName the new workflow name
     * @return the cloned workflow
     */
    public Workflow clone(String workflowId, String newName) {
        return clone(workflowId, newName, false);
    }
    
    /**
     * Clone a workflow with options.
     *
     * @param workflowId the workflow ID
     * @param newName the new workflow name
     * @param includeHistory whether to include execution history
     * @return the cloned workflow
     */
    public Workflow clone(String workflowId, String newName, boolean includeHistory) {
        String url = getBaseUrl() + "/" + workflowId + "/clone?newName=" + encodeParam(newName) + "&includeHistory=" + includeHistory;
        return httpClient.postWithCustomBase(url, new HashMap<>(), Workflow.class);
    }
    
    /**
     * Export a workflow.
     *
     * @param workflowId the workflow ID
     * @return the exported workflow data
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> export(String workflowId) {
        return export(workflowId, "json", true);
    }
    
    /**
     * Export a workflow with options.
     *
     * @param workflowId the workflow ID
     * @param format the export format
     * @param includeMetadata whether to include metadata
     * @return the exported workflow data
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> export(String workflowId, String format, boolean includeMetadata) {
        String url = getBaseUrl() + "/" + workflowId + "/export?format=" + format + "&includeMetadata=" + includeMetadata;
        return httpClient.getWithCustomBase(url, Map.class);
    }
    
    /**
     * Get workflow analytics.
     *
     * @param workflowId the workflow ID
     * @return analytics data
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getAnalytics(String workflowId) {
        return getAnalytics(workflowId, 30, false);
    }
    
    /**
     * Get workflow analytics with options.
     *
     * @param workflowId the workflow ID
     * @param days number of days
     * @param detailed whether to include detailed analytics
     * @return analytics data
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getAnalytics(String workflowId, int days, boolean detailed) {
        String url = getBaseUrl() + "/" + workflowId + "/analytics?days=" + days + "&detailed=" + detailed;
        return httpClient.getWithCustomBase(url, Map.class);
    }
    
    /**
     * Search workflows.
     *
     * @param query the search query
     * @return list of matching workflows
     */
    public List<Workflow> search(String query) {
        return search(query, 0, 20);
    }
    
    /**
     * Search workflows with pagination.
     *
     * @param query the search query
     * @param page the page number
     * @param size the page size
     * @return list of matching workflows
     */
    public List<Workflow> search(String query, int page, int size) {
        String url = getBaseUrl() + "/search?query=" + encodeParam(query) + "&page=" + page + "&size=" + size;
        WorkflowListResponse response = httpClient.getWithCustomBase(url, WorkflowListResponse.class);
        return response.getWorkflowList();
    }
    
    /**
     * Link an agent to a workflow.
     *
     * @param workflowId the workflow ID
     * @param agentId the agent ID
     */
    public void linkAgent(String workflowId, String agentId) {
        httpClient.postWithCustomBase(
            getBaseUrl() + "/" + workflowId + "/agent/" + agentId,
            new HashMap<>(),
            Void.class
        );
    }
    
    /**
     * Unlink an agent from a workflow.
     *
     * @param workflowId the workflow ID
     * @param agentId the agent ID
     */
    public void unlinkAgent(String workflowId, String agentId) {
        httpClient.deleteWithCustomBase(getBaseUrl() + "/" + workflowId + "/agent/" + agentId);
    }
    
    private String encodeParam(String param) {
        try {
            return java.net.URLEncoder.encode(param, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return param;
        }
    }
    
    private List<Map<String, Object>> convertNodes(List<WorkflowNode> nodes) {
        if (nodes == null) return new ArrayList<>();
        List<Map<String, Object>> result = new ArrayList<>();
        for (WorkflowNode node : nodes) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", node.getId());
            map.put("type", node.getType());
            if (node.getName() != null) map.put("name", node.getName());
            if (node.getPosition() != null) {
                Map<String, Double> pos = new HashMap<>();
                pos.put("x", node.getPosition().getX());
                pos.put("y", node.getPosition().getY());
                map.put("position", pos);
            }
            if (node.getConfig() != null) map.put("config", node.getConfig());
            result.add(map);
        }
        return result;
    }
    
    private List<Map<String, Object>> convertEdges(List<WorkflowEdge> edges) {
        if (edges == null) return new ArrayList<>();
        List<Map<String, Object>> result = new ArrayList<>();
        for (WorkflowEdge edge : edges) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", edge.getId());
            map.put("source", edge.getSource());
            map.put("target", edge.getTarget());
            if (edge.getCondition() != null) map.put("condition", edge.getCondition());
            result.add(map);
        }
        return result;
    }
}







