package com.swfte.sdk.unit.resources;

import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.models.MCPServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class McpTest {

    private TestServer server;
    private SwfteClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new TestServer("{\"providerId\":\"github-mcp\"}");
        client = SwfteClient.builder()
            .apiKey("sk-swfte-test")
            .workspaceId("ws-test")
            .baseUrl(server.baseUrl())
            .maxRetries(1)
            .build();
    }

    @AfterEach
    void tearDown() { server.close(); }

    @Test
    void shouldExposeMcpViaClient() {
        assertNotNull(client.mcp());
    }

    @Test
    void connectPostsToConnectPath() {
        MCPServer out = client.mcp().connect(
            MCPServer.builder()
                .providerId("github-mcp")
                .url("https://mcp.example.com")
                .transport("sse")
                .build()
        );
        assertEquals("POST", server.last().method);
        assertEquals("/api/v2/mcp/servers/connect", server.last().path);
        assertEquals("github-mcp", out.getProviderId());
    }

    @Test
    void executeToolPostsToExecutePath() {
        client.mcp().executeTool("github-mcp.get-issue", Map.of("number", 42));
        assertEquals("POST", server.last().method);
        assertEquals("/api/v2/mcp/tools/github-mcp.get-issue/execute", server.last().path);
    }

    @Test
    void disconnectSendsDelete() {
        client.mcp().disconnect("github-mcp");
        assertEquals("DELETE", server.last().method);
        assertEquals("/api/v2/mcp/servers/github-mcp", server.last().path);
    }

    @Test
    void analyticsGetsAnalyticsPath() {
        client.mcp().analytics();
        assertEquals("GET", server.last().method);
        assertEquals("/api/v2/mcp/analytics", server.last().path);
    }
}
