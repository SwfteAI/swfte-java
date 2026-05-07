# Cookbook: Modules

The `Modules` resource bundles agents, workflows, tools, datasets, and chatflows into reusable, versionable units that can be published to the marketplace. Endpoints live under `/v2/modules`. See [swfte.com/marketplace](https://www.swfte.com/marketplace).

```java
import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.models.Module;
import com.swfte.sdk.models.ModuleVersion;

import java.util.Map;

public class ModulesExample {
    public static void main(String[] args) {
        SwfteClient client = SwfteClient.builder()
            .apiKey(System.getenv("SWFTE_API_KEY"))
            .workspaceId("ws-12345")              // replace with your own
            .build();

        // CREATE
        Module module = client.modules().create(
            Module.builder()
                .name("Sales Suite")
                .description("Lead capture chatflow + qualification agent + Slack notifier")
                .build()
        );

        // ADD RESOURCES
        client.modules().addResource(module.getId(), Map.of(
            "type", "AGENT",
            "id",   "agent-12345"            // replace with your own
        ));
        client.modules().addResource(module.getId(), Map.of(
            "type", "CHATFLOW",
            "id",   "chatflow-12345"          // replace with your own
        ));
        client.modules().addResource(module.getId(), Map.of(
            "type", "WORKFLOW",
            "id",   "workflow-12345"          // replace with your own
        ));

        // BUILD a versioned bundle
        Map<String, Object> build = client.modules().build(module.getId());
        System.out.println("Build started: " + build.get("buildId"));

        // SSE progress stream URL — subscribe with your favourite SSE client
        String progressUrl = client.modules().buildProgressUrl(module.getId());
        System.out.println("Subscribe to: " + progressUrl);

        // VERSIONS
        client.modules().listVersions(module.getId());
        ModuleVersion v1 = client.modules().getVersion(module.getId(), "1");
        client.modules().versionQa(module.getId(), v1.getVersion());

        // IMPACT
        client.modules().impact(module.getId());

        // UPDATE / REMOVE / DELETE
        client.modules().update(module.getId(),
            Module.builder().description("EU + UK ready").build());
        client.modules().removeResource(module.getId(), "agent-12345");
        client.modules().delete(module.getId());
    }
}
```

Full reference at [swfte.com/developers](https://www.swfte.com/developers).
