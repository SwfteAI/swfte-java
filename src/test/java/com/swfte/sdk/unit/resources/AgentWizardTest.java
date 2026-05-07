package com.swfte.sdk.unit.resources;

import com.swfte.sdk.SwfteClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AgentWizardTest {

    private TestServer server;
    private SwfteClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new TestServer("{\"draftId\":\"draft-12345\"}");
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
    void shouldExposeAgentWizardViaClient() {
        assertNotNull(client.agentWizard());
    }

    @Test
    void generatePostsToWizardGenerate() {
        client.agentWizard().generate(Map.of("prompt", "research assistant"));
        assertEquals("POST", server.last().method);
        assertEquals("/v2/agents/wizard/generate", server.last().path);
    }

    @Test
    void templatesGetsTemplates() {
        client.agentWizard().templates();
        assertEquals("GET", server.last().method);
        assertEquals("/v2/agents/wizard/templates", server.last().path);
    }

    @Test
    void fromTemplatePostsToFromTemplatePath() {
        client.agentWizard().fromTemplate("support-bot", null);
        assertEquals("POST", server.last().method);
        assertEquals("/v2/agents/wizard/from-template/support-bot", server.last().path);
    }
}
