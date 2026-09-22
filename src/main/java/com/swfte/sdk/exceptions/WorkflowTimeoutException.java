package com.swfte.sdk.exceptions;

import com.swfte.sdk.models.WorkflowExecution;

/**
 * Polling gave up before the workflow run finished. The run is NOT cancelled;
 * poll {@link #getExecutionId()} again to follow it.
 */
public class WorkflowTimeoutException extends SwfteException {
    private final String executionId;
    private final transient WorkflowExecution lastStatus;

    public WorkflowTimeoutException(String message, String executionId, WorkflowExecution lastStatus) {
        super(message);
        this.executionId = executionId;
        this.lastStatus = lastStatus;
    }

    public String getExecutionId() {
        return executionId;
    }

    public WorkflowExecution getLastStatus() {
        return lastStatus;
    }
}
