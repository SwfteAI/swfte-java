package com.swfte.sdk.unit.resources;

import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.models.Module;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ModulesTest {

    private TestServer server;
    private SwfteClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new TestServer("{\"id\":\"module-12345\",\"name\":\"Sales Suite\"}");
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
    void shouldExposeModulesViaClient() {
        assertNotNull(client.modules());
    }

    @Test
    void createPostsToV2Modules() {
        Module out = client.modules().create(Module.builder().name("Sales Suite").build());
        assertEquals("POST", server.last().method);
        assertEquals("/v2/modules", server.last().path);
        assertEquals("module-12345", out.getId());
    }

    @Test
    void buildPostsBuildPath() {
        client.modules().build("module-12345");
        assertEquals("POST", server.last().method);
        assertEquals("/v2/modules/module-12345/build", server.last().path);
    }

    @Test
    void getVersionUsesVersionsPath() {
        client.modules().getVersion("module-12345", "1");
        assertEquals("GET", server.last().method);
        assertEquals("/v2/modules/module-12345/versions/1", server.last().path);
    }

    @Test
    void impactGetsImpactPath() {
        client.modules().impact("module-12345");
        assertEquals("GET", server.last().method);
        assertEquals("/v2/modules/module-12345/impact", server.last().path);
    }
}
