# Cookbook: Agents

The `Agents` resource manages AI agents — stateful, tool-using LLM workers with system prompts, models, capability tiers, and per-agent canvas/session metadata. Endpoints live under `/v2/agents`.

```java
import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.models.Agent;

import java.util.List;

public class AgentsExample {
    public static void main(String[] args) {
        SwfteClient client = SwfteClient.builder()
            .apiKey(System.getenv("SWFTE_API_KEY"))
            .workspaceId("ws-12345")            // replace with your own
            .build();

        // CREATE
        Agent agent = client.agents().create(
            Agent.builder()
                .agentName("Research Assistant")
                .description("Summarises arxiv papers and finds related work")
                .systemPrompt("You are a meticulous research assistant.")
                .provider("openai")
                .model("gpt-4")
                .temperature(0.3)
                .maxTokens(2048)
                .build()
        );
        System.out.println("Created agent: " + agent.getId());

        // GET
        Agent fetched = client.agents().get(agent.getId());

        // LIST
        List<Agent> agents = client.agents().list();
        System.out.println("Workspace has " + agents.size() + " agents");

        // PATCH (V2)
        Agent updated = client.agents().update(agent.getId(),
            Agent.builder().description("Updated focus: NLP arxiv only").build()
        );

        // TOGGLE active
        client.agents().toggleActive(agent.getId(), false);

        // ASSOCIATE a workflow
        client.agents().associateWorkflow(agent.getId(), "workflow-12345"); // replace with your own

        // DELETE
        client.agents().delete(agent.getId());
    }
}
```

Full reference at [swfte.com/developers](https://www.swfte.com/developers).
