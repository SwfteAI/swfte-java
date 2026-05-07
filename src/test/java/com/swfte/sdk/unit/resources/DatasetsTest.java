package com.swfte.sdk.unit.resources;

import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.models.Dataset;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class DatasetsTest {

    private TestServer server;
    private SwfteClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new TestServer("{\"id\":\"dataset-12345\",\"name\":\"Product Docs\"}");
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
    void shouldExposeDatasetsViaClient() {
        assertNotNull(client.datasets());
    }

    @Test
    void createPostsToApiV2Datasets() {
        Dataset out = client.datasets().create(Dataset.builder().name("Product Docs").build());
        assertEquals("POST", server.last().method);
        assertEquals("/api/v2/datasets", server.last().path);
        assertEquals("dataset-12345", out.getId());
    }

    @Test
    void toggleApiAccessPostsToStatusPath() {
        client.datasets().toggleApiAccess("dataset-12345", "ENABLED");
        assertEquals("POST", server.last().method);
        assertEquals("/api/v2/datasets/dataset-12345/api-access/ENABLED", server.last().path);
    }

    @Test
    void useCheckGets() {
        client.datasets().useCheck("dataset-12345");
        assertEquals("GET", server.last().method);
        assertEquals("/api/v2/datasets/dataset-12345/use-check", server.last().path);
    }
}
