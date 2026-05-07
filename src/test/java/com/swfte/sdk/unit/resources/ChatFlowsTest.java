package com.swfte.sdk.unit.resources;

import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.models.ChatFlow;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ChatFlows resource.
 */
class ChatFlowsTest {

    private TestServer server;
    private SwfteClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new TestServer("{\"id\":\"flow-12345\",\"name\":\"Lead Capture\"}");
        client = SwfteClient.builder()
            .apiKey("sk-swfte-test")
            .workspaceId("ws-test")
            .baseUrl(server.baseUrl())
            .maxRetries(1)
            .build();
    }

    @AfterEach
    void tearDown() {
        server.close();
    }

    @Test
    void shouldExposeChatFlowsResourceViaClient() {
        assertNotNull(client.chatflows());
    }

    @Test
    void createSendsPostToV2Chatflows() {
        ChatFlow result = client.chatflows().create(
            ChatFlow.builder().name("Lead Capture").description("test").build()
        );

        TestServer.Recorded last = server.last();
        assertEquals("POST", last.method);
        assertEquals("/v2/chatflows", last.path);
        assertNotNull(last.authorization);
        assertEquals("ws-test", last.workspaceId);
        assertEquals("flow-12345", result.getId());
    }

    @Test
    void getSendsGetToV2ChatflowsId() {
        client.chatflows().get("flow-12345");
        assertEquals("GET", server.last().method);
        assertEquals("/v2/chatflows/flow-12345", server.last().path);
    }

    @Test
    void deploySendsPostToDeployPath() {
        client.chatflows().deploy("flow-12345");
        assertEquals("POST", server.last().method);
        assertEquals("/v2/chatflows/flow-12345/deploy", server.last().path);
    }

    @Test
    void deleteSendsDelete() {
        client.chatflows().delete("flow-12345");
        assertEquals("DELETE", server.last().method);
        assertEquals("/v2/chatflows/flow-12345", server.last().path);
    }

    @Test
    void builderTemplatesHitsBuilderPath() {
        client.chatflows().builder().templates();
        assertEquals("GET", server.last().method);
        assertEquals("/v2/chatflows/builder/templates", server.last().path);
    }

    @Test
    void versionsListHitsVersionsPath() {
        client.chatflows().versions("flow-12345").list();
        assertEquals("GET", server.last().method);
        assertEquals("/v2/chatflows/flow-12345/versions", server.last().path);
    }

    @Test
    void publishingPublishHitsPublishPath() {
        client.chatflows().publishing().publish("flow-12345", null);
        assertEquals("POST", server.last().method);
        assertEquals("/v2/chatflows/flow-12345/publish", server.last().path);
    }
}
