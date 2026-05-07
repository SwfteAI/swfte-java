package com.swfte.sdk.unit.resources;

import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.models.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DocumentsTest {

    private TestServer server;
    private SwfteClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new TestServer("{\"documents\":[]}");
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
    void shouldExposeDocumentsViaClient() {
        assertNotNull(client.documents());
    }

    @Test
    void createPostsDocumentsList() {
        client.documents().create("dataset-12345", List.of(
            Document.builder().name("Pricing FAQ").dataSourceType("TEXT").content("hi").build()
        ));
        assertEquals("POST", server.last().method);
        assertEquals("/api/v2/datasets/dataset-12345/documents", server.last().path);
    }

    @Test
    void retryPostsToRetryPath() {
        client.documents().retry("dataset-12345", "doc-9");
        assertEquals("POST", server.last().method);
        assertEquals("/api/v2/datasets/dataset-12345/documents/doc-9/retry", server.last().path);
    }

    @Test
    void segmentsGets() {
        client.documents().segments("dataset-12345", "doc-9");
        assertEquals("GET", server.last().method);
        assertEquals("/api/v2/datasets/dataset-12345/documents/doc-9/segments", server.last().path);
    }
}
