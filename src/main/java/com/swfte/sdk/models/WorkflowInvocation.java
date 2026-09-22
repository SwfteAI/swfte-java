package com.swfte.sdk.models;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Response of {@code POST /v2/workflows/{id}/invoke} (HTTP 202): the run was accepted.
 */
public class WorkflowInvocation {
    private final String executionId;
    private final String workflowId;
    private final String status;
    private final Map<String, Object> raw;

    public WorkflowInvocation(String executionId, String workflowId, String status, Map<String, Object> raw) {
        this.executionId = executionId;
        this.workflowId = workflowId;
        this.status = status;
        this.raw = raw == null ? Collections.emptyMap() : Collections.unmodifiableMap(new LinkedHashMap<>(raw));
    }

    public String getExecutionId() {
        return executionId;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    /** Usually {@code PENDING}. */
    public String getStatus() {
        return status;
    }

    public Map<String, Object> getRaw() {
        return raw;
    }
}
