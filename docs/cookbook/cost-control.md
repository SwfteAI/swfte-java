# Cookbook: Cost Control

The `CostControl` resource manages model routing rules, per-workspace and per-model usage caps, and scaling configuration for self-hosted deployments. Endpoints live under `/v2/cost-control`. See [swfte.com/products/cost-control](https://www.swfte.com/products/cost-control).

```java
import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.models.RoutingRule;
import com.swfte.sdk.models.UsageCap;

import java.util.List;
import java.util.Map;

public class CostControlExample {
    public static void main(String[] args) {
        SwfteClient client = SwfteClient.builder()
            .apiKey(System.getenv("SWFTE_API_KEY"))
            .workspaceId("ws-12345")              // replace with your own
            .build();

        // ROUTING RULES — fall over from a high-cost model to a cheaper one
        // when latency or error-rate exceeds a threshold.
        RoutingRule rule = client.costControl().createRoutingRule(
            RoutingRule.builder()
                .name("Cap GPT-4 latency over 5s")
                .priority(10)
                .enabled(true)
                .matchModel("openai:gpt-4")
                .fallbackModel("anthropic:claude-haiku")
                .condition(Map.of(
                    "metric",   "p95_latency_ms",
                    "operator", ">",
                    "value",    5000,
                    "window",   "5m"
                ))
                .actions(List.of(
                    Map.of("type", "REROUTE"),
                    Map.of("type", "NOTIFY", "channel", "EMAIL")
                ))
                .build()
        );

        client.costControl().listRoutingRules();
        client.costControl().getRoutingRule(rule.getId());
        client.costControl().toggleRoutingRule(rule.getId(), false);

        // USAGE CAPS — workspace-wide budget
        client.costControl().setWorkspaceCap(
            UsageCap.builder()
                .budgetUsd(2500.0)
                .period("MONTHLY")
                .onExceededAction("THROTTLE")
                .enforced(true)
                .build()
        );

        // MODEL-LEVEL cap
        client.costControl().setModelCap("openai:gpt-4",
            UsageCap.builder()
                .budgetUsd(500.0)
                .period("MONTHLY")
                .onExceededAction("REJECT")
                .enforced(true)
                .build()
        );

        client.costControl().listUsageCaps();
        client.costControl().usageStats();

        // SCALING for self-hosted GPU deployments
        client.costControl().scalingConfig("deployment-12345"); // replace with your own

        // CLEANUP
        client.costControl().deleteRoutingRule(rule.getId());
    }
}
```

Full reference at [swfte.com/developers](https://www.swfte.com/developers).
