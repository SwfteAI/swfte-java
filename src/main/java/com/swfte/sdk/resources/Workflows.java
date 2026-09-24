package com.swfte.sdk.resources;

import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.HttpClient;
import com.swfte.sdk.models.Workflow;
import com.swfte.sdk.models.WorkflowNode;
import com.swfte.sdk.models.WorkflowEdge;
import com.swfte.sdk.models.WorkflowExecution;
import com.swfte.sdk.models.WorkflowListResponse;
import com.swfte.sdk.models.WorkflowInvocation;
import com.swfte.sdk.exceptions.ApiException;
import com.swfte.sdk.exceptions.SwfteException;
import com.swfte.sdk.exceptions.WorkflowExecutionException;
import com.swfte.sdk.exceptions.WorkflowPausedException;
import com.swfte.sdk.exceptions.WorkflowTimeoutException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
 * // Production: run the PUBLISHED version and wait for it
 * Map<String, Object> inputs = new HashMap<>();
 * inputs.put("message", "Hello!");
 * WorkflowExecution result = client.workflows().invokeAndWait(workflow.getId(), inputs);
 * System.out.println(result.getStatusRaw() + " " + result.getOutputs());
 *
 * // Test run of the current (draft) definition
 * WorkflowExecution execution = client.workflows().execute(workflow.getId(), inputs);
 * WorkflowExecution done = client.workflows().waitForCompletion(execution.getExecutionId());
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
     * Run the workflow's CURRENT (editable/draft) definition — Studio's test path.
     *
     * <p>{@code POST /v2/workflows/{id}/execute}. The server refuses (409
     * {@code WORKFLOW_NOT_PUBLISHED}) a workflow that was never published unless
     * the inputs carry {@code testingFlag: true}. Production callers should use
     * {@link #invoke(String, Map)}, which runs the published snapshot.</p>
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
     * Run the workflow's PUBLISHED snapshot — the production path.
     *
     * <p>{@code POST /v2/workflows/{id}/invoke} with the inputs as the JSON body. The
     * server answers 202 with an {@code executionId} once the run is queued. A
     * never-published workflow answers 409 ({@code PUBLISHED_SNAPSHOT_UNAVAILABLE}),
     * thrown as {@link ApiException} with status 409; {@code testingFlag} is rejected (400).
     * Not retried: a retry could start the run twice.</p>
     *
     * @param workflowId the workflow ID
     * @param inputs the workflow inputs ({@code null} sends {@code {}})
     * @return the accepted invocation, carrying the execution ID
     */
    @SuppressWarnings("unchecked")
    public WorkflowInvocation invoke(String workflowId, Map<String, Object> inputs) {
        if (workflowId == null || workflowId.isEmpty()) {
            throw new SwfteException("workflowId is required");
        }
        Map<String, Object> res = httpClient.apiRequest(
            "POST",
            getBaseUrl() + "/" + encode(workflowId) + "/invoke",
            inputs != null ? inputs : new HashMap<>(),
            Map.class
        );
        Object executionId = res == null ? null : res.get("executionId");
        if (executionId == null || String.valueOf(executionId).isEmpty()) {
            throw new ApiException("Invoke response did not include an executionId", 502, String.valueOf(res));
        }
        Object wf = res.get("workflowId");
        Object status = res.get("status");
        return new WorkflowInvocation(
            String.valueOf(executionId),
            wf == null ? null : String.valueOf(wf),
            status == null ? null : String.valueOf(status),
            res
        );
    }

    /**
     * Invoke the published workflow and poll until it finishes (5 min timeout, 2 s interval).
     *
     * @see #invokeAndWait(String, Map, long, long)
     */
    public WorkflowExecution invokeAndWait(String workflowId, Map<String, Object> inputs) {
        return invokeAndWait(workflowId, inputs, 300000, 2000);
    }

    /**
     * Invoke the published workflow and poll until the run reaches a terminal status.
     *
     * @param workflowId the workflow ID
     * @param inputs the workflow inputs
     * @param timeoutMs give up after this long (client side; the run keeps going)
     * @param pollIntervalMs delay between status polls
     * @return the final execution when it succeeded ({@code SUCCESS}, {@code SUCCEEDED} or {@code COMPLETED}),
     *         or — returned at once rather than polled until the timeout — the execution paused for
     *         human input ({@code PAUSED}, {@code WAITING_FOR_INPUT}; {@link WorkflowExecution#isPaused()}
     *         true, {@link WorkflowExecution#getWaitingFor()} names the gate)
     * @throws WorkflowExecutionException the run ended FAILED/TIMEOUT or CANCELLED/CANCELED
     * @throws WorkflowTimeoutException {@code timeoutMs} elapsed first; the run is not cancelled
     */
    public WorkflowExecution invokeAndWait(String workflowId, Map<String, Object> inputs, long timeoutMs, long pollIntervalMs) {
        return invokeAndWait(workflowId, inputs, timeoutMs, pollIntervalMs, false);
    }

    /**
     * {@link #invokeAndWait(String, Map, long, long)}, throwing {@link WorkflowPausedException}
     * instead of returning when the run pauses for input and {@code throwOnPause} is true.
     */
    public WorkflowExecution invokeAndWait(String workflowId, Map<String, Object> inputs, long timeoutMs, long pollIntervalMs,
                                           boolean throwOnPause) {
        WorkflowInvocation invocation = invoke(workflowId, inputs);
        return pollUntilTerminal(invocation.getExecutionId(), timeoutMs, pollIntervalMs, throwOnPause);
    }

    /**
     * Get execution status.
     *
     * <p>{@code GET /v2/workflows/executions/{executionId}/status}. The nested
     * {@code execution} record is lifted; see {@link WorkflowExecution#fromStatusResponse}.</p>
     *
     * @param executionId the execution ID
     * @return the workflow execution
     */
    @SuppressWarnings("unchecked")
    public WorkflowExecution getExecutionStatus(String executionId) {
        if (executionId == null || executionId.isEmpty()) {
            throw new SwfteException("executionId is required");
        }
        Map<String, Object> body = httpClient.apiRequest(
            "GET",
            getBaseUrl() + "/executions/" + encode(executionId) + "/status",
            null,
            Map.class
        );
        return WorkflowExecution.fromStatusResponse(executionId, body);
    }

    private WorkflowExecution pollUntilTerminal(String executionId, long timeoutMs, long pollIntervalMs, boolean throwOnPause) {
        long deadline = System.nanoTime() + Math.max(0, timeoutMs) * 1_000_000L;
        long interval = Math.max(0, pollIntervalMs);
        while (true) { // always polls at least once, even with timeoutMs = 0
            WorkflowExecution execution = getExecutionStatus(executionId);
            String status = execution.getStatusRaw();
            switch (execution.getOutcome()) {
                case SUCCEEDED:
                    return execution;
                case PAUSED: {
                    // BT-N5: a human-in-the-loop run will not finish by being polled; hand it back now.
                    if (!throwOnPause) return execution;
                    List<WorkflowExecution.PausedNode> waiting = execution.getWaitingFor();
                    StringBuilder where = new StringBuilder();
                    for (WorkflowExecution.PausedNode n : waiting) where.append(where.length() == 0 ? " at " : ", ").append(n.getNodeId());
                    throw new WorkflowPausedException(
                        "Execution " + executionId + " is waiting for input (" + status + where + ")",
                        executionId, status, waiting, execution);
                }
                case FAILED:
                    throw new WorkflowExecutionException(
                        "Execution " + executionId + " " + status.toLowerCase(java.util.Locale.ROOT)
                            + (execution.getError() != null ? ": " + execution.getError() : ""),
                        executionId, status, execution);
                case CANCELLED:
                    throw new WorkflowExecutionException(
                        "Execution " + executionId + " was cancelled", executionId, status, execution);
                default:
                    break;
            }
            long remainingMs = (deadline - System.nanoTime()) / 1_000_000L;
            if (remainingMs <= 0) {
                throw new WorkflowTimeoutException(
                    "Execution " + executionId + " did not complete within " + timeoutMs + "ms (last status "
                        + (status.isEmpty() ? "unknown" : status) + ")",
                    executionId, execution);
            }
            try {
                Thread.sleep(Math.min(interval, remainingMs));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new SwfteException("Interrupted while waiting for execution " + executionId, e);
            }
        }
    }

    private static String encode(String segment) {
        try {
            return URLEncoder.encode(segment, StandardCharsets.UTF_8.name()).replace("+", "%20");
        } catch (java.io.UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
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
     * Wait for an existing execution (from {@link #execute} or {@link #invoke}) to finish.
     * Same terminal rules as {@link #invokeAndWait(String, Map, long, long)}.
     *
     * @param executionId the execution ID
     * @param timeoutMs timeout in milliseconds
     * @param pollIntervalMs poll interval in milliseconds
     * @return the completed execution
     * @throws WorkflowExecutionException if the execution fails or is cancelled
     * @throws WorkflowTimeoutException if it does not finish within {@code timeoutMs}
     */
    public WorkflowExecution waitForCompletion(String executionId, long timeoutMs, long pollIntervalMs) {
        return pollUntilTerminal(executionId, timeoutMs, pollIntervalMs, false);
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







