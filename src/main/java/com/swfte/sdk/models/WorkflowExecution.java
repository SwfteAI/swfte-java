package com.swfte.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Represents a workflow execution.
 *
 * <p>{@link #fromStatusResponse(String, Map)} builds one from the status endpoint's
 * {@code {"execution": {...}, "nodeExecutions": [...], "progress": n}} shape by lifting
 * the nested record. {@link #getStatusRaw()} is the server's status string even when it
 * is not a {@link Status} member.</p>
 */
public class WorkflowExecution {

    /**
     * Execution status. The server has used {@code SUCCESS}, {@code SUCCEEDED} and
     * {@code COMPLETED} for a successful run and both {@code CANCELLED} and
     * {@code CANCELED}; all are members.
     */
    public enum Status {
        PENDING,
        RUNNING,
        PAUSED,
        COMPLETED,
        SUCCESS,
        SUCCEEDED,
        FAILED,
        TIMEOUT,
        CANCELLED,
        CANCELED
    }

    /** Where an execution sits: still running, or which terminal outcome. */
    public enum Outcome {
        RUNNING,
        SUCCEEDED,
        FAILED,
        CANCELLED
    }

    /** Statuses meaning the run finished successfully. */
    public static final Set<String> SUCCESS_STATUSES =
        Collections.unmodifiableSet(new HashSet<>(Arrays.asList("SUCCESS", "SUCCEEDED", "COMPLETED")));
    /** Statuses meaning the run finished unsuccessfully. */
    public static final Set<String> FAILURE_STATUSES =
        Collections.unmodifiableSet(new HashSet<>(Arrays.asList("FAILED", "ERROR", "TIMEOUT", "TIMED_OUT")));
    /** Statuses meaning the run was cancelled (both spellings). */
    public static final Set<String> CANCELLED_STATUSES =
        Collections.unmodifiableSet(new HashSet<>(Arrays.asList("CANCELLED", "CANCELED")));

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Classify a raw status string (case-insensitive); unknown or missing is {@link Outcome#RUNNING}. */
    public static Outcome classify(String status) {
        String s = status == null ? "" : status.toUpperCase(Locale.ROOT);
        if (SUCCESS_STATUSES.contains(s)) return Outcome.SUCCEEDED;
        if (FAILURE_STATUSES.contains(s)) return Outcome.FAILED;
        if (CANCELLED_STATUSES.contains(s)) return Outcome.CANCELLED;
        return Outcome.RUNNING;
    }

    private String id;
    private String executionId;
    private String workflowId;
    private Status status;
    private Double progress;
    private Map<String, Object> inputs;
    private Map<String, Object> outputs;
    private String error;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    @JsonIgnore
    private String statusRaw;
    @JsonIgnore
    private List<Map<String, Object>> nodeExecutions;
    @JsonIgnore
    private Map<String, Object> raw;
    
    public WorkflowExecution() {}
    
    // Getters and Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getExecutionId() {
        return executionId;
    }
    
    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }
    
    public String getWorkflowId() {
        return workflowId;
    }
    
    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public Double getProgress() {
        return progress;
    }
    
    public void setProgress(Double progress) {
        this.progress = progress;
    }
    
    public Map<String, Object> getInputs() {
        return inputs;
    }
    
    public void setInputs(Map<String, Object> inputs) {
        this.inputs = inputs;
    }
    
    public Map<String, Object> getOutputs() {
        return outputs;
    }
    
    public void setOutputs(Map<String, Object> outputs) {
        this.outputs = outputs;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public LocalDateTime getStartedAt() {
        return startedAt;
    }
    
    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    /** The server's status string, upper-cased (falls back to {@link #getStatus()}). */
    public String getStatusRaw() {
        if (statusRaw != null && !statusRaw.isEmpty()) return statusRaw;
        return status == null ? "" : status.name();
    }

    public void setStatusRaw(String statusRaw) {
        this.statusRaw = statusRaw;
    }

    @JsonIgnore
    public Outcome getOutcome() {
        return classify(getStatusRaw());
    }

    @JsonIgnore
    public boolean isTerminal() {
        return getOutcome() != Outcome.RUNNING;
    }

    @JsonIgnore
    public boolean isSucceeded() {
        return getOutcome() == Outcome.SUCCEEDED;
    }

    public List<Map<String, Object>> getNodeExecutions() {
        return nodeExecutions;
    }

    public void setNodeExecutions(List<Map<String, Object>> nodeExecutions) {
        this.nodeExecutions = nodeExecutions;
    }

    /** The full status response body, when built by {@link #fromStatusResponse(String, Map)}. */
    public Map<String, Object> getRaw() {
        return raw;
    }

    public void setRaw(Map<String, Object> raw) {
        this.raw = raw;
    }

    /**
     * Build from {@code GET /v2/workflows/executions/{executionId}/status}, lifting
     * {@code executionId}, {@code workflowId}, {@code status}, {@code outputData}
     * and the error message out of the nested {@code execution} record.
     */
    @SuppressWarnings("unchecked")
    public static WorkflowExecution fromStatusResponse(String executionId, Map<String, Object> body) {
        Map<String, Object> data = body == null ? Collections.emptyMap() : body;
        Map<String, Object> nested = data.get("execution") instanceof Map
            ? (Map<String, Object>) data.get("execution")
            : Collections.emptyMap();

        WorkflowExecution e = new WorkflowExecution();
        String id = str(pick(data, nested, "executionId", "id"));
        e.setExecutionId(id != null ? id : executionId);
        e.setId(e.getExecutionId());
        e.setWorkflowId(str(pick(data, nested, "workflowId")));

        String status = str(pick(data, nested, "status"));
        String upper = status == null ? "" : status.toUpperCase(Locale.ROOT);
        e.setStatusRaw(upper);
        try {
            e.setStatus(upper.isEmpty() ? null : Status.valueOf(upper));
        } catch (IllegalArgumentException ignored) {
            e.setStatus(null);
        }

        Object progress = data.containsKey("progress") ? data.get("progress") : nested.get("progress");
        if (progress instanceof Number) {
            e.setProgress(((Number) progress).doubleValue());
        }

        Object outputs = data.get("outputs");
        if (outputs == null) outputs = nested.get("outputData");
        if (outputs == null) outputs = nested.get("outputs");
        if (outputs instanceof Map) {
            e.setOutputs((Map<String, Object>) outputs);
        } else if (outputs != null) {
            e.setOutputs(Collections.singletonMap("value", outputs));
        }

        Object inputs = pick(data, nested, "inputs", "inputData");
        if (inputs instanceof Map) {
            e.setInputs((Map<String, Object>) inputs);
        }

        Object error = data.get("error");
        if (error == null && nested.get("errorInfo") instanceof Map) {
            Map<String, Object> info = (Map<String, Object>) nested.get("errorInfo");
            error = info.get("message") != null ? info.get("message") : info.get("errorMessage");
        }
        if (error == null) error = nested.get("errorMessage");
        if (error == null) error = nested.get("error");
        if (error != null) {
            e.setError(error instanceof String ? (String) error : json(error));
        }

        if (data.get("nodeExecutions") instanceof List) {
            e.setNodeExecutions((List<Map<String, Object>>) data.get("nodeExecutions"));
        }
        e.setRaw(data);
        return e;
    }

    private static Object pick(Map<String, Object> a, Map<String, Object> b, String... keys) {
        for (String k : keys) {
            if (a.get(k) != null) return a.get(k);
            if (b.get(k) != null) return b.get(k);
        }
        return null;
    }

    private static String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private static String json(Object o) {
        try {
            return MAPPER.writeValueAsString(o);
        } catch (JsonProcessingException ex) {
            return String.valueOf(o);
        }
    }
}
