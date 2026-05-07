# Cookbook: Workflows

The `Workflows` resource manages durable DAG executions with LLM, HTTP, tool, condition, loop, and human-in-the-loop nodes. Endpoints live under `/v2/workflows`. See [swfte.com/products/workflows](https://www.swfte.com/products/workflows).

```java
import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.models.Workflow;
import com.swfte.sdk.models.WorkflowEdge;
import com.swfte.sdk.models.WorkflowExecution;
import com.swfte.sdk.models.WorkflowNode;

import java.util.List;
import java.util.Map;

public class WorkflowsExample {
    public static void main(String[] args) {
        SwfteClient client = SwfteClient.builder()
            .apiKey(System.getenv("SWFTE_API_KEY"))
            .workspaceId("ws-12345")              // replace with your own
            .build();

        // CREATE
        Workflow wf = client.workflows().create(
            Workflow.builder()
                .name("Content Pipeline")
                .description("LLM enrichment + HTTP publish")
                .nodes(List.of(
                    WorkflowNode.builder().id("start").type("TRIGGER").build(),
                    WorkflowNode.builder().id("llm").type("LLM").build(),
                    WorkflowNode.builder().id("publish").type("HTTP").build(),
                    WorkflowNode.builder().id("end").type("END").build()
                ))
                .edges(List.of(
                    new WorkflowEdge("e1", "start", "llm"),
                    new WorkflowEdge("e2", "llm", "publish"),
                    new WorkflowEdge("e3", "publish", "end")
                ))
                .build()
        );

        // GET / LIST
        client.workflows().get(wf.getId());
        client.workflows().list();
        client.workflows().search("content");

        // UPDATE / VALIDATE / CLONE
        client.workflows().validate(wf);
        Workflow cloned = client.workflows().clone(wf.getId(), "Content Pipeline (EU)");

        // EXECUTE + WAIT
        WorkflowExecution exec = client.workflows().execute(wf.getId(),
            Map.of("topic", "weekly digest"));
        WorkflowExecution result = client.workflows().waitForCompletion(exec.getId());
        System.out.println("Status: " + result.getStatus());

        // ANALYTICS
        client.workflows().getAnalytics(wf.getId(), 30, true);

        // EXPORT
        client.workflows().export(wf.getId());

        // CLEANUP
        client.workflows().delete(cloned.getId());
        client.workflows().delete(wf.getId());
    }
}
```

Full reference at [swfte.com/developers](https://www.swfte.com/developers).
