package com.swfte.sdk.unit.resources;

import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.models.RoutingRule;
import com.swfte.sdk.models.UsageCap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class CostControlTest {

    private TestServer server;
    private SwfteClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new TestServer("{\"id\":\"rule-12345\",\"name\":\"Cap GPT-4 latency\"}");
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
    void shouldExposeCostControlViaClient() {
        assertNotNull(client.costControl());
    }

    @Test
    void createRoutingRulePostsToRulesPath() {
        RoutingRule out = client.costControl().createRoutingRule(
            RoutingRule.builder().name("Cap GPT-4 latency").priority(10).enabled(true).build()
        );
        assertEquals("POST", server.last().method);
        assertEquals("/v2/cost-control/routing-rules", server.last().path);
        assertEquals("rule-12345", out.getId());
    }

    @Test
    void toggleRoutingRuleSendsPatch() {
        client.costControl().toggleRoutingRule("rule-12345", false);
        assertEquals("PATCH", server.last().method);
        assertEquals("/v2/cost-control/routing-rules/rule-12345/toggle", server.last().path);
    }

    @Test
    void setWorkspaceCapPutsToWorkspacePath() {
        client.costControl().setWorkspaceCap(
            UsageCap.builder().budgetUsd(2500.0).period("MONTHLY").enforced(true).build()
        );
        assertEquals("PUT", server.last().method);
        assertEquals("/v2/cost-control/usage-caps/workspace", server.last().path);
    }

    @Test
    void setModelCapPutsToModelPath() {
        client.costControl().setModelCap("openai:gpt-4",
            UsageCap.builder().budgetUsd(500.0).build()
        );
        assertEquals("PUT", server.last().method);
        assertEquals("/v2/cost-control/usage-caps/model/openai:gpt-4", server.last().path);
    }
}
