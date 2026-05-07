package com.swfte.sdk.unit.resources;

import com.swfte.sdk.SwfteClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class FilesTest {

    private TestServer server;
    private SwfteClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new TestServer("{\"id\":\"file-12345\",\"name\":\"invoice.pdf\"}");
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
    void shouldExposeFilesViaClient() {
        assertNotNull(client.files());
    }

    @Test
    void configHitsConfigPath() {
        client.files().config();
        assertEquals("GET", server.last().method);
        assertEquals("/api/v2/files/config", server.last().path);
    }

    @Test
    void deleteHitsFileIdPath() {
        client.files().delete("file-12345");
        assertEquals("DELETE", server.last().method);
        assertEquals("/api/v2/files/file-12345", server.last().path);
    }

    @Test
    void cleanupPosts() {
        client.files().cleanup();
        assertEquals("POST", server.last().method);
        assertEquals("/api/v2/files/cleanup", server.last().path);
    }
}
