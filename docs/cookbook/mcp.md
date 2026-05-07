# Cookbook: MCP

The `Mcp` resource lets your code connect Model Context Protocol servers, list and execute tools, and view analytics. Endpoints live under `/api/v2/mcp`. See [swfte.com/products/mcp](https://www.swfte.com/products/mcp).

```java
import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.models.MCPServer;
import com.swfte.sdk.models.MCPTool;

import java.util.List;
import java.util.Map;

public class McpExample {
    public static void main(String[] args) {
        SwfteClient client = SwfteClient.builder()
            .apiKey(System.getenv("SWFTE_API_KEY"))
            .workspaceId("ws-12345")              // replace with your own
            .build();

        // CONNECT a server
        MCPServer server = client.mcp().connect(
            MCPServer.builder()
                .providerId("github-mcp")
                .name("GitHub MCP")
                .url("https://mcp.example.com/github")
                .transport("sse")
                .auth(Map.of("type", "bearer", "token", "ghp_replace_me"))
                .build()
        );

        // LIST servers and tools
        client.mcp().listServers();
        client.mcp().listTools();

        // INSPECT a tool's schema
        MCPTool schema = client.mcp().toolSchema("github-mcp.get-issue");
        System.out.println("Schema: " + schema.getInputSchema());

        // EXECUTE a single tool
        Map<String, Object> result = client.mcp().executeTool(
            "github-mcp.get-issue",
            Map.of("repo", "swfteai/swfte-java", "number", 42)
        );

        // BATCH execute
        client.mcp().batchExecute(List.of(
            Map.of("toolId", "github-mcp.get-issue", "args", Map.of("number", 42)),
            Map.of("toolId", "github-mcp.list-prs",  "args", Map.of("state", "open"))
        ));

        // OBSERVABILITY
        client.mcp().analytics();
        client.mcp().healthCheck();
        client.mcp().toolStatus("github-mcp.get-issue");

        // DISCONNECT
        client.mcp().disconnect(server.getProviderId());
    }
}
```

Full reference at [swfte.com/developers](https://www.swfte.com/developers).
