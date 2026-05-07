# Cookbook: Audit

The `Audit` resource queries the platform-wide, per-workspace audit log — every action recorded, exportable, and queryable. Endpoints live under `/v2/audit`. See [swfte.com/products/security](https://www.swfte.com/products/security).

```java
import com.swfte.sdk.SwfteClient;

import java.util.Map;

public class AuditExample {
    public static void main(String[] args) {
        SwfteClient client = SwfteClient.builder()
            .apiKey(System.getenv("SWFTE_API_KEY"))
            .workspaceId("ws-12345")              // replace with your own
            .build();

        // LIST EVENTS with filters
        client.audit().listEvents(Map.of(
            "from",   "2026-05-01T00:00:00Z",
            "to",     "2026-05-07T23:59:59Z",
            "action", "AGENT_UPDATED"
        ));

        // EVENTS for a single resource
        client.audit().resourceEvents("agent", "agent-12345");      // replace with your own
        client.audit().resourceEvents("chatflow", "chatflow-12345"); // replace with your own

        // EVENTS attributed to the current authenticated principal
        client.audit().myEvents();

        // EXPORT — returns a download URL or inline payload depending on size
        Map<String, Object> export = client.audit().export(Map.of(
            "from",   "2026-05-01T00:00:00Z",
            "to",     "2026-05-07T23:59:59Z",
            "format", "json"
        ));
        System.out.println("Export ready: " + export.get("downloadUrl"));
    }
}
```

Full reference at [swfte.com/developers](https://www.swfte.com/developers).
