package com.swfte.sdk.unit.resources;

import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.models.Installation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class MarketplaceTest {

    private TestServer server;
    private SwfteClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new TestServer("{\"id\":\"inst-12345\"}");
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
    void shouldExposeMarketplaceViaClient() {
        assertNotNull(client.marketplace());
    }

    @Test
    void browseGetsMarketplaceRoot() {
        client.marketplace().browse();
        assertEquals("GET", server.last().method);
        assertEquals("/v2/marketplace", server.last().path);
    }

    @Test
    void installPostsToInstallPath() {
        Installation out = client.marketplace().install("pub-12345", null);
        assertEquals("POST", server.last().method);
        assertEquals("/v2/marketplace/pub-12345/install", server.last().path);
        assertEquals("inst-12345", out.getId());
    }

    @Test
    void uninstallSendsDelete() {
        client.marketplace().uninstall("inst-12345");
        assertEquals("DELETE", server.last().method);
        assertEquals("/v2/marketplace/installations/inst-12345", server.last().path);
    }
}
