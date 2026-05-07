package com.swfte.sdk.unit.resources;

import com.swfte.sdk.SwfteClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AuditTest {

    private TestServer server;
    private SwfteClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new TestServer("{\"events\":[]}");
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
    void shouldExposeAuditViaClient() {
        assertNotNull(client.audit());
    }

    @Test
    void listEventsHitsEventsPath() {
        client.audit().listEvents(null);
        assertEquals("GET", server.last().method);
        assertEquals("/v2/audit/events", server.last().path);
    }

    @Test
    void listEventsAppliesFiltersAsQueryString() {
        Map<String, Object> filters = new LinkedHashMap<>();
        filters.put("actorId", "user-123");
        filters.put("from", "2026-05-01T00:00:00Z");
        client.audit().listEvents(filters);
        assertEquals("GET", server.last().method);
        // URL-encoded colons + ordering preserved by LinkedHashMap
        assertTrue(server.last().path.startsWith("/v2/audit/events?"));
        assertTrue(server.last().path.contains("actorId=user-123"));
        assertTrue(server.last().path.contains("from=2026-05-01T00"));
    }

    @Test
    void resourceEventsHitsResourcePath() {
        client.audit().resourceEvents("agent", "agent-12345");
        assertEquals("GET", server.last().method);
        assertEquals("/v2/audit/events/agent/agent-12345", server.last().path);
    }

    @Test
    void myEventsHitsMePath() {
        client.audit().myEvents();
        assertEquals("GET", server.last().method);
        assertEquals("/v2/audit/events/me", server.last().path);
    }
}
