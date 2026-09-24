package com.swfte.sdk.exceptions;

import com.swfte.sdk.models.WorkflowExecution;

import java.util.Collections;
import java.util.List;

/**
 * A workflow run stopped to wait for a person (a HUMAN_INPUT gate) or an external
 * event. Thrown only by the {@code throwOnPause = true} overloads; by default
 * {@code invokeAndWait} returns the execution with {@link WorkflowExecution#isPaused()}
 * true. The run has not failed: resume it (Studio, or {@code resumeExecution}) and
 * poll {@link #getExecutionId()} again.
 */
public class WorkflowPausedException extends SwfteException {
    private final String executionId;
    private final String status;
    private final transient List<WorkflowExecution.PausedNode> waitingFor;
    private final transient WorkflowExecution execution;

    public WorkflowPausedException(String message, String executionId, String status,
                                   List<WorkflowExecution.PausedNode> waitingFor, WorkflowExecution execution) {
        super(message);
        this.executionId = executionId;
        this.status = status;
        this.waitingFor = waitingFor == null ? Collections.emptyList() : waitingFor;
        this.execution = execution;
    }

    public String getExecutionId() {
        return executionId;
    }

    public String getStatus() {
        return status;
    }

    /** The node(s) the run waits on, typically a HUMAN_INPUT gate. */
    public List<WorkflowExecution.PausedNode> getWaitingFor() {
        return waitingFor;
    }

    public WorkflowExecution getExecution() {
        return execution;
    }
}
