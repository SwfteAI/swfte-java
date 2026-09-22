package com.swfte.sdk.exceptions;

import com.swfte.sdk.models.WorkflowExecution;

/**
 * A workflow run reached a terminal status other than success
 * (FAILED, TIMEOUT, CANCELLED/CANCELED). {@link #getExecution()} holds the final status.
 */
public class WorkflowExecutionException extends SwfteException {
    private final String executionId;
    private final String status;
    private final transient WorkflowExecution execution;

    public WorkflowExecutionException(String message, String executionId, String status, WorkflowExecution execution) {
        super(message);
        this.executionId = executionId;
        this.status = status;
        this.execution = execution;
    }

    public String getExecutionId() {
        return executionId;
    }

    /** The terminal status as the server reported it, e.g. {@code FAILED} or {@code CANCELED}. */
    public String getStatus() {
        return status;
    }

    public WorkflowExecution getExecution() {
        return execution;
    }
}
