package com.swfte.sdk.unit.resources;

import com.swfte.sdk.SwfteClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class VoiceCallsTest {

    private TestServer server;
    private SwfteClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new TestServer("{\"callSid\":\"CAabcdef\"}");
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
    void shouldExposeVoiceCallsViaClient() {
        assertNotNull(client.voiceCalls());
    }

    @Test
    void inProgressGetsInProgressPath() {
        client.voiceCalls().inProgress();
        assertEquals("GET", server.last().method);
        assertEquals("/v2/voice/calls/in-progress", server.last().path);
    }

    @Test
    void getReturnsCall() {
        client.voiceCalls().get("CAabcdef");
        assertEquals("GET", server.last().method);
        assertEquals("/v2/voice/calls/CAabcdef", server.last().path);
    }

    @Test
    void transcriptHitsTranscriptPath() {
        client.voiceCalls().transcript("CAabcdef");
        assertEquals("GET", server.last().method);
        assertEquals("/v2/voice/calls/CAabcdef/transcript", server.last().path);
    }
}
