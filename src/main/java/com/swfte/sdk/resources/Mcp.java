package com.swfte.sdk.resources;

import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.HttpClient;
import com.swfte.sdk.models.MCPServer;
import com.swfte.sdk.models.MCPTool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model Context Protocol (MCP) API — connect MCP servers, discover and
 * execute tools, view analytics.
 *
 * <p>See <a href="https://www.swfte.com/products/mcp">swfte.com/products/mcp</a>.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * MCPServer server = client.mcp().connect(
 *     MCPServer.builder()
 *         .providerId("github-mcp")
 *         .url("https://mcp.example.com")
 *         .transport("sse")
 *         .build()
 * );
 *
 * Map<String, Object> result = client.mcp().executeTool(
 *     "github-mcp.get-issue",
 *     Map.of("repo", "swfteai/swfte-java", "number", 42)
 * );
 * }</pre>
 */
public class Mcp {

    private final HttpClient httpClient;
    private final SwfteClient client;

    public Mcp(SwfteClient client) {
        this.client = client;
        this.httpClient = new HttpClient(client);
    }

    private String base() { return "/api/v2/mcp"; }

    /** Connect an MCP server. */
    public MCPServer connect(MCPServer server) {
        return httpClient.postWithCustomBase(base() + "/servers/connect", server, MCPServer.class);
    }

    /** List connected MCP servers. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> listServers() {
        return httpClient.getWithCustomBase(base() + "/servers", Map.class);
    }

    /** Disconnect an MCP server. */
    public void disconnect(String providerId) {
        httpClient.deleteWithCustomBase(base() + "/servers/" + providerId);
    }

    /** List all available MCP tools. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> listTools() {
        return httpClient.getWithCustomBase(base() + "/tools", Map.class);
    }

    /** Get the schema for a single tool. */
    public MCPTool toolSchema(String toolId) {
        return httpClient.getWithCustomBase(base() + "/tools/" + toolId + "/schema", MCPTool.class);
    }

    /** Execute a single MCP tool. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> executeTool(String toolId, Map<String, Object> args) {
        return httpClient.postWithCustomBase(
            base() + "/tools/" + toolId + "/execute",
            args != null ? args : new HashMap<>(),
            Map.class
        );
    }

    /** Get tool usage analytics. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> analytics() {
        return httpClient.getWithCustomBase(base() + "/analytics", Map.class);
    }

    /** Run a health check across connected MCP servers. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> healthCheck() {
        return httpClient.postWithCustomBase(base() + "/health-check", new HashMap<>(), Map.class);
    }

    /** Execute several MCP tools in one call. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> batchExecute(List<Map<String, Object>> calls) {
        Map<String, Object> body = new HashMap<>();
        body.put("calls", calls);
        return httpClient.postWithCustomBase(base() + "/tools/batch-execute", body, Map.class);
    }

    /** Get the runtime status of a tool. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> toolStatus(String toolId) {
        return httpClient.getWithCustomBase(base() + "/tools/" + toolId + "/status", Map.class);
    }
}
