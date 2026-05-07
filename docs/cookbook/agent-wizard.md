# Cookbook: Agent Wizard

The `AgentWizard` resource generates, reviews, refines, and persists agents from a natural-language prompt or a named template. Endpoints live under `/v2/agents/wizard`.

```java
import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.models.Agent;

import java.util.Map;

public class AgentWizardExample {
    public static void main(String[] args) {
        SwfteClient client = SwfteClient.builder()
            .apiKey(System.getenv("SWFTE_API_KEY"))
            .workspaceId("ws-12345")              // replace with your own
            .build();

        // 1. Generate a draft from a prompt
        Map<String, Object> draft = client.agentWizard().generate(Map.of(
            "prompt", "An email triage assistant that classifies and drafts replies",
            "providerHint", "anthropic"
        ));

        // 2. Review and refine
        Map<String, Object> reviewed = client.agentWizard().review(Map.of("draftId", draft.get("draftId")));
        Map<String, Object> refined  = client.agentWizard().refine(Map.of(
            "draftId", draft.get("draftId"),
            "feedback", "Tighten tone, classify into Urgent/Normal/Spam"
        ));

        // 3. Persist
        Agent created = client.agentWizard().create(refined);
        System.out.println("Persisted agent: " + created.getId());

        // 4. Discover what's available
        client.agentWizard().templates();
        client.agentWizard().agentTypes();
        client.agentWizard().providers();

        // 5. Or skip the dialog and clone a template
        Agent fromTemplate = client.agentWizard().fromTemplate(
            "support-tier-1",                             // replace with your own
            Map.of("name", "Support Bot — EU")
        );

        // 6. Wire knowledge bases and MCP tools onto the agent
        client.agentWizard().linkTools(Map.of(
            "agentId", created.getId(),
            "toolIds", java.util.List.of("github-mcp.list-issues") // replace with your own
        ));
        client.agentWizard().linkKnowledge(Map.of(
            "agentId", created.getId(),
            "datasetIds", java.util.List.of("dataset-12345")        // replace with your own
        ));
    }
}
```

Full reference at [swfte.com/developers](https://www.swfte.com/developers).
