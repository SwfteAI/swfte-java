package com.swfte.sdk.unit.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.exceptions.ApiException;
import com.swfte.sdk.exceptions.AuthenticationException;
import com.swfte.sdk.exceptions.RateLimitException;
import com.swfte.sdk.exceptions.SwfteException;
import com.swfte.sdk.exceptions.WorkflowExecutionException;
import com.swfte.sdk.exceptions.WorkflowPausedException;
import com.swfte.sdk.exceptions.WorkflowTimeoutException;
import com.swfte.sdk.models.AgentChatOptions;
import com.swfte.sdk.models.AgentChatResponse;
import com.swfte.sdk.models.CatalogContract;
import com.swfte.sdk.models.CatalogEntry;
import com.swfte.sdk.models.CatalogSearchParams;
import com.swfte.sdk.models.CatalogSearchResponse;
import com.swfte.sdk.models.WorkflowExecution;
import com.swfte.sdk.models.WorkflowInvocation;
import com.swfte.sdk.unit.resources.TestServer.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * agents().chat, workflows().invoke / getExecutionStatus / invokeAndWait, catalog().
 *
 * <p>The client is configured with a gateway URL ({@code <server>/v2/gateway}) so
 * every assertion also proves the calls land on the agents-service root, not the
 * gateway.</p>
 */
class SotInvokeTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private TestServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.close();
        }
    }

    private SwfteClient client(Response... responses) throws Exception {
        server = new TestServer(responses);
        return SwfteClient.builder()
            .apiKey("pat_abc")
            .workspaceId("ws_9")
            .baseUrl(server.baseUrl() + "/v2/gateway")
            .maxRetries(3)
            .build();
    }

    private static Response ok(String json) {
        return new Response(200, json);
    }

    private static String statusBody(String status, String extraExecutionJson) {
        return "{\"execution\":{\"executionId\":\"ex_1\",\"workflowId\":\"wf_1\",\"status\":\"" + status + "\""
            + (extraExecutionJson == null ? "" : "," + extraExecutionJson)
            + "},\"nodeExecutions\":[],\"progress\":" + ("RUNNING".equals(status) ? 50 : 100) + "}";
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> json(String body) throws Exception {
        return MAPPER.readValue(body, Map.class);
    }

    @Nested
    class BaseUrl {
        @Test
        void derivesAgentsRootFromDefaultGateway() {
            SwfteClient c = SwfteClient.builder().apiKey("k").build();
            assertEquals("https://api.swfte.com/agents/v2/gateway", c.getBaseUrl());
            assertEquals("https://api.swfte.com/agents", c.getApiBaseUrl());
        }

        @Test
        void derivationRules() {
            assertEquals("http://localhost:8080", SwfteClient.deriveApiBaseUrl("http://localhost:8080/v1/gateway/"));
            assertEquals("https://proxy.test/agents", SwfteClient.deriveApiBaseUrl("https://proxy.test/agents"));
        }

        @Test
        void explicitApiBaseUrlWins() {
            SwfteClient c = SwfteClient.builder().apiKey("k").apiBaseUrl("http://local:1/").build();
            assertEquals("http://local:1", c.getApiBaseUrl());
        }
    }

    @Nested
    class AgentChat {
        @Test
        void postsMessageToChatPathWithHeaders() throws Exception {
            SwfteClient c = client(ok("{\"response\":\"Hi there\",\"conversationId\":\"conv_1\",\"model\":\"m\"}"));
            AgentChatResponse reply = c.agents().chat("ag_1", "Hello",
                AgentChatOptions.builder().userId("user-42").build());

            assertEquals(1, server.recorded().size());
            assertEquals("POST", server.last().method);
            assertEquals("/v1/agents/ag_1/chat/user-42", server.last().path);
            assertEquals(Map.of("message", "Hello"), json(server.last().body));
            assertEquals("Bearer pat_abc", server.last().authorization);
            assertEquals("ws_9", server.last().workspaceId);
            assertEquals("Hi there", reply.getResponse());
            assertEquals("conv_1", reply.getConversationId());
            assertEquals("m", reply.getModel());
        }

        @Test
        void defaultsUserIdAndForwardsConversationId() throws Exception {
            SwfteClient c = client(ok("{\"response\":\"again\"}"));
            c.agents().chat("ag_1", "More", AgentChatOptions.builder().conversationId("conv_1").build());
            assertEquals("/v1/agents/ag_1/chat/sdk-user", server.last().path);
            assertEquals(Map.of("message", "More", "conversationId", "conv_1"), json(server.last().body));

            c.agents().chat("ag_1", "Plain");
            assertEquals("/v1/agents/ag_1/chat/sdk-user", server.last().path);
            assertEquals(Map.of("message", "Plain"), json(server.last().body));
        }

        @Test
        void normalisesContentToResponse() throws Exception {
            SwfteClient c = client(ok("{\"content\":\"from content\",\"conversationId\":\"c\"}"));
            AgentChatResponse reply = c.agents().chat("ag_1", "x");
            assertEquals("from content", reply.getResponse());
            assertEquals("from content", reply.getRaw().get("content"));
        }

        @Test
        void encodesPathSegments() throws Exception {
            SwfteClient c = client(ok("{\"response\":\"ok\"}"));
            c.agents().chat("ag/1", "x", AgentChatOptions.builder().userId("a b@c").build());
            assertEquals("/v1/agents/ag%2F1/chat/a%20b%40c", server.last().path);
        }

        @Test
        void mapsErrorsAndDoesNotRetry() throws Exception {
            SwfteClient c = client(new Response(503, "{\"error\":\"AGENT_EXECUTION_FAILED\"}"));
            ApiException e = assertThrows(ApiException.class, () -> c.agents().chat("ag_1", "x"));
            assertEquals(503, e.getStatusCode());
            assertTrue(e.getResponseBody().contains("AGENT_EXECUTION_FAILED"));
            assertEquals(1, server.recorded().size());
        }

        @Test
        void mapsAuthAndRateLimit() throws Exception {
            SwfteClient c = client(new Response(401, "{}"), new Response(403, "{}"), new Response(429, "{}"));
            assertThrows(AuthenticationException.class, () -> c.agents().chat("ag_1", "x"));
            assertThrows(AuthenticationException.class, () -> c.agents().chat("ag_1", "x"));
            assertThrows(RateLimitException.class, () -> c.agents().chat("ag_1", "x"));
            assertEquals(3, server.recorded().size());
        }

        @Test
        void rejectsEmptyMessageLocally() throws Exception {
            SwfteClient c = client(ok("{}"));
            assertThrows(SwfteException.class, () -> c.agents().chat("ag_1", ""));
            assertEquals(0, server.recorded().size());
        }
    }

    @Nested
    class WorkflowInvoke {
        @Test
        void invokePostsInputsToInvokePath() throws Exception {
            SwfteClient c = client(new Response(202, "{\"executionId\":\"ex_1\",\"workflowId\":\"wf_1\",\"status\":\"PENDING\"}"));
            Map<String, Object> inputs = new LinkedHashMap<>();
            inputs.put("topic", "x");
            inputs.put("n", 2);
            WorkflowInvocation inv = c.workflows().invoke("wf_1", inputs);

            assertEquals("POST", server.last().method);
            assertEquals("/v2/workflows/wf_1/invoke", server.last().path);
            assertEquals(Map.of("topic", "x", "n", 2), json(server.last().body));
            assertEquals("Bearer pat_abc", server.last().authorization);
            assertEquals("ex_1", inv.getExecutionId());
            assertEquals("PENDING", inv.getStatus());
        }

        @Test
        void invokeSendsEmptyObjectForNullInputs() throws Exception {
            SwfteClient c = client(new Response(202, "{\"executionId\":\"ex_1\"}"));
            c.workflows().invoke("wf_1", null);
            assertEquals(Map.of(), json(server.last().body));
        }

        @Test
        void invoke409IsApiExceptionAndNotRetried() throws Exception {
            SwfteClient c = client(new Response(409, "{\"error\":\"PUBLISHED_SNAPSHOT_UNAVAILABLE\"}"));
            ApiException e = assertThrows(ApiException.class, () -> c.workflows().invoke("wf_1", new HashMap<>()));
            assertEquals(409, e.getStatusCode());
            assertEquals(1, server.recorded().size());
        }

        @Test
        void invokeWithoutExecutionIdFails() throws Exception {
            SwfteClient c = client(new Response(202, "{\"status\":\"PENDING\"}"));
            assertThrows(ApiException.class, () -> c.workflows().invoke("wf_1", null));
        }

        @Test
        void getExecutionStatusLiftsNestedRecord() throws Exception {
            SwfteClient c = client(ok(statusBody("success", "\"outputData\":{\"answer\":42}")));
            WorkflowExecution st = c.workflows().getExecutionStatus("ex_1");

            assertEquals("GET", server.last().method);
            assertEquals("/v2/workflows/executions/ex_1/status", server.last().path);
            assertEquals("", server.last().body);
            assertEquals("SUCCESS", st.getStatusRaw());
            assertEquals(WorkflowExecution.Status.SUCCESS, st.getStatus());
            assertTrue(st.isSucceeded());
            assertEquals("ex_1", st.getExecutionId());
            assertEquals("wf_1", st.getWorkflowId());
            assertEquals(42, st.getOutputs().get("answer"));
            assertEquals(100.0, st.getProgress());
        }

        @Test
        void classifiesBothSpellings() {
            for (String s : new String[] {"SUCCESS", "SUCCEEDED", "COMPLETED", "succeeded"}) {
                assertEquals(WorkflowExecution.Outcome.SUCCEEDED, WorkflowExecution.classify(s), s);
            }
            for (String s : new String[] {"FAILED", "TIMEOUT", "ERROR"}) {
                assertEquals(WorkflowExecution.Outcome.FAILED, WorkflowExecution.classify(s), s);
            }
            for (String s : new String[] {"CANCELLED", "CANCELED"}) {
                assertEquals(WorkflowExecution.Outcome.CANCELLED, WorkflowExecution.classify(s), s);
            }
            for (String s : new String[] {"PENDING", "RUNNING", "", null}) {
                assertEquals(WorkflowExecution.Outcome.RUNNING, WorkflowExecution.classify(s), String.valueOf(s));
            }
        }

        @Test
        void invokeAndWaitPollsUntilSucceeded() throws Exception {
            SwfteClient c = client(
                new Response(202, "{\"executionId\":\"ex_1\"}"),
                ok(statusBody("PENDING", null)),
                ok(statusBody("RUNNING", null)),
                ok(statusBody("SUCCEEDED", "\"outputData\":{\"ok\":true}")));
            WorkflowExecution done = c.workflows().invokeAndWait("wf_1", Map.of("a", 1), 5000, 1);

            assertEquals("SUCCEEDED", done.getStatusRaw());
            assertEquals(true, done.getOutputs().get("ok"));
            assertEquals(4, server.recorded().size());
            assertEquals("POST", server.recorded().get(0).method);
            assertEquals("/v2/workflows/wf_1/invoke", server.recorded().get(0).path);
            assertEquals(Map.of("a", 1), json(server.recorded().get(0).body));
            for (int i = 1; i <= 3; i++) {
                assertEquals("GET", server.recorded().get(i).method);
                assertEquals("/v2/workflows/executions/ex_1/status", server.recorded().get(i).path);
            }
        }

        @Test
        void invokeAndWaitThrowsOnFailed() throws Exception {
            SwfteClient c = client(
                new Response(202, "{\"executionId\":\"ex_1\"}"),
                ok(statusBody("FAILED", "\"errorInfo\":{\"message\":\"node llm_1 exploded\"}")));
            WorkflowExecutionException e = assertThrows(WorkflowExecutionException.class,
                () -> c.workflows().invokeAndWait("wf_1", null, 5000, 1));
            assertEquals("FAILED", e.getStatus());
            assertEquals("ex_1", e.getExecutionId());
            assertTrue(e.getMessage().contains("node llm_1 exploded"));
            assertEquals(2, server.recorded().size());
        }

        @Test
        void invokeAndWaitThrowsOnCanceled() throws Exception {
            SwfteClient c = client(new Response(202, "{\"executionId\":\"ex_1\"}"), ok(statusBody("CANCELED", null)));
            WorkflowExecutionException e = assertThrows(WorkflowExecutionException.class,
                () -> c.workflows().invokeAndWait("wf_1", null, 5000, 1));
            assertEquals("CANCELED", e.getStatus());
        }

        @Test
        void invokeAndWaitStopsAtDeadline() throws Exception {
            SwfteClient c = client(new Response(202, "{\"executionId\":\"ex_1\"}"), ok(statusBody("RUNNING", null)));
            long started = System.nanoTime();
            WorkflowTimeoutException e = assertThrows(WorkflowTimeoutException.class,
                () -> c.workflows().invokeAndWait("wf_1", null, 60, 10));
            long elapsedMs = (System.nanoTime() - started) / 1_000_000L;
            assertEquals("ex_1", e.getExecutionId());
            assertTrue(elapsedMs < 2000, "took " + elapsedMs + "ms");
            int calls = server.recorded().size();
            assertTrue(calls >= 2 && calls < 20, "calls=" + calls);
        }

        @Test
        void timeoutZeroStillPollsOnce() throws Exception {
            SwfteClient c = client(new Response(202, "{\"executionId\":\"ex_1\"}"), ok(statusBody("SUCCESS", null)));
            assertTrue(c.workflows().invokeAndWait("wf_1", null, 0, 0).isSucceeded());
        }

        @Test
        void statusErrorPropagates() throws Exception {
            SwfteClient c = client(new Response(202, "{\"executionId\":\"ex_1\"}"), new Response(404, "{\"error\":\"nope\"}"));
            ApiException e = assertThrows(ApiException.class, () -> c.workflows().invokeAndWait("wf_1", null, 5000, 1));
            assertEquals(404, e.getStatusCode());
            assertEquals(2, server.recorded().size());
        }

        @Test
        void waitForCompletionAcceptsSuccess() throws Exception {
            SwfteClient c = client(ok(statusBody("SUCCESS", null)));
            assertTrue(c.workflows().waitForCompletion("ex_1", 1000, 0).isSucceeded());
        }

        @Test
        void executeStillUsesDraftPath() throws Exception {
            SwfteClient c = client(ok("{\"executionId\":\"ex_2\",\"status\":\"PENDING\"}"));
            c.workflows().execute("wf_1", Map.of("a", 1));
            assertEquals("/v2/workflows/wf_1/execute", server.last().path);
        }
    }

    @Nested
    class CatalogApi {
        @Test
        void searchSendsFiltersAsQuery() throws Exception {
            SwfteClient c = client(ok("{\"items\":[{\"catalogRef\":\"workflow:wf_1\",\"kind\":\"workflow\",\"id\":\"wf_1\","
                + "\"facets\":[{\"key\":\"domain\",\"value\":\"finance\",\"confidence\":0.9,\"status\":\"PROPOSED\",\"source\":\"jev\"}],"
                + "\"evidence\":{\"level\":\"corroborated\",\"runs\":{\"total\":6,\"succeeded\":6,\"failed\":0},\"reasons\":[\"6 runs\"]}}],"
                + "\"nextCursor\":\"c2\",\"degraded\":[\"jev_rerank\"]}"));
            CatalogSearchResponse res = c.catalog().search(CatalogSearchParams.builder()
                .q("invoice triage")
                .kinds("workflow", "agent")
                .scope("all")
                .minEvidence("corroborated")
                .limit(5)
                .build());

            assertEquals("GET", server.last().method);
            assertEquals("/v2/catalog/search?q=invoice%20triage&kinds=workflow%2Cagent&scope=all"
                + "&minEvidence=corroborated&limit=5", server.last().path);
            assertEquals("", server.last().body);
            assertEquals(1, res.getItems().size());
            CatalogEntry item = res.getItems().get(0);
            assertEquals("workflow:wf_1", item.getCatalogRef());
            assertEquals("finance", item.getFacets().get(0).getValue());
            assertEquals("corroborated", item.getEvidence().getLevel());
            assertEquals(6, item.getEvidence().getRuns().get("total"));
            assertEquals("c2", res.getNextCursor());
            assertEquals("jev_rerank", res.getDegraded().get(0));
        }

        @Test
        void searchWithoutFiltersHitsBarePath() throws Exception {
            SwfteClient c = client(ok("{}"));
            CatalogSearchResponse res = c.catalog().search();
            assertEquals("/v2/catalog/search", server.last().path);
            assertTrue(res.getItems().isEmpty());
            assertNull(res.getNextCursor());
            assertTrue(res.getDegraded().isEmpty());
        }

        @Test
        void getAndContract() throws Exception {
            SwfteClient c = client(
                ok("{\"catalogRef\":\"mcp-server:m 1\",\"reviews\":[{\"verdict\":\"approve\"}]}"),
                ok("{\"catalogRef\":\"workflow:wf_1\",\"invoke\":{\"method\":\"POST\",\"path\":\"/v2/workflows/wf_1/invoke\","
                    + "\"auth\":\"pat\",\"async\":true,\"statusPath\":\"/v2/workflows/executions/{executionId}/status\"},"
                    + "\"inputSchema\":{\"type\":\"object\"},\"snippets\":{\"curl\":\"curl ...\"},\"embed\":null}"));

            CatalogEntry detail = c.catalog().get("mcp-server", "m 1");
            assertEquals("GET", server.last().method);
            assertEquals("/v2/catalog/mcp-server/m%201", server.last().path);
            assertEquals("approve", detail.getReviews().get(0).get("verdict"));

            CatalogContract contract = c.catalog().contract("workflow", "wf_1");
            assertEquals("/v2/catalog/workflow/wf_1/contract", server.last().path);
            assertEquals("ws_9", server.last().workspaceId);
            assertEquals("POST", contract.getInvoke().getMethod());
            assertEquals("/v2/workflows/wf_1/invoke", contract.getInvoke().getPath());
            assertTrue(contract.getInvoke().isAsync());
            assertEquals("object", contract.getInputSchema().get("type"));
            assertEquals("curl ...", contract.getSnippets().get("curl"));
            assertNull(contract.getEmbed());
        }

        @Test
        void notFoundIsApiException() throws Exception {
            SwfteClient c = client(new Response(404, "{\"error\":\"not found\"}"));
            ApiException e = assertThrows(ApiException.class, () -> c.catalog().get("workflow", "nope"));
            assertEquals(404, e.getStatusCode());
        }
    }

    /** Battle-test regressions (BATTLE_TEST.md N5, N12, N13). */
    @Nested
    class BattleRegressions {
        @Test
        void pausedAndWaitingForInputClassifyAsPaused() {
            for (String st : new String[] {"PAUSED", "WAITING_FOR_INPUT", "AWAITING_HUMAN", "awaiting_input", "WAITING"}) {
                assertEquals(WorkflowExecution.Outcome.PAUSED, WorkflowExecution.classify(st), st);
            }
            assertTrue(WorkflowExecution.PAUSED_STATUSES.contains("WAITING_FOR_INPUT"));
        }

        @Test
        void invokeAndWaitReturnsPromptlyWhenPausedForInput() throws Exception {
            String paused = "{\"execution\":{\"executionId\":\"ex_1\",\"workflowId\":\"wf_1\",\"status\":\"WAITING_FOR_INPUT\"},"
                + "\"nodeExecutions\":[{\"nodeId\":\"start\",\"nodeType\":\"START\",\"status\":\"SUCCEEDED\"},"
                + "{\"nodeId\":\"approve_1\",\"nodeType\":\"HUMAN_INPUT\",\"status\":\"PAUSED\",\"pauseReason\":\"HumanInputRequired\"}],\"progress\":50}";
            SwfteClient c = client(new Response(202, "{\"executionId\":\"ex_1\"}"), ok(statusBody("RUNNING", null)), ok(paused));
            long started = System.nanoTime();
            WorkflowExecution res = c.workflows().invokeAndWait("wf_1", null, 300000, 1);
            assertTrue((System.nanoTime() - started) / 1_000_000L < 2000, "burned the timeout");
            assertEquals(3, server.recorded().size());
            assertTrue(res.isPaused());
            assertFalse(res.isTerminal());
            assertEquals(WorkflowExecution.Outcome.PAUSED, res.getOutcome());
            assertEquals("WAITING_FOR_INPUT", res.getStatusRaw());
            List<WorkflowExecution.PausedNode> waiting = res.getWaitingFor();
            assertEquals(1, waiting.size());
            assertEquals("approve_1", waiting.get(0).getNodeId());
            assertEquals("HUMAN_INPUT", waiting.get(0).getNodeType());
            assertEquals("HumanInputRequired", waiting.get(0).getReason());
        }

        @Test
        void backendSpellingPausedIsPausedToo() throws Exception {
            SwfteClient c = client(new Response(202, "{\"executionId\":\"ex_1\"}"), ok(statusBody("PAUSED", null)));
            WorkflowExecution res = c.workflows().invokeAndWait("wf_1", null, 300000, 1);
            assertTrue(res.isPaused());
            assertEquals(WorkflowExecution.Status.PAUSED, res.getStatus());
        }

        @Test
        void throwOnPauseRaisesWorkflowPausedException() throws Exception {
            SwfteClient c = client(new Response(202, "{\"executionId\":\"ex_1\"}"), ok(statusBody("WAITING_FOR_INPUT", null)));
            WorkflowPausedException e = assertThrows(WorkflowPausedException.class,
                () -> c.workflows().invokeAndWait("wf_1", null, 300000, 1, true));
            assertEquals("ex_1", e.getExecutionId());
            assertEquals("WAITING_FOR_INPUT", e.getStatus());
        }

        @Test
        void waitForCompletionReturnsEarlyOnPause() throws Exception {
            SwfteClient c = client(ok(statusBody("PAUSED", null)));
            assertTrue(c.workflows().waitForCompletion("ex_1", 300000, 1).isPaused());
        }

        @Test
        void chatPrefersContentOverLegacyResponse() throws Exception {
            SwfteClient c = client(ok("{\"content\":\"A\",\"response\":\"B\",\"conversationId\":\"c\"}"));
            assertEquals("A", c.agents().chat("ag_1", "hi").getResponse());
        }

        @Test
        void deriveApiBaseUrlStripsBareGateway() {
            assertEquals("https://x/agents", SwfteClient.deriveApiBaseUrl("https://x/agents/gateway"));
            assertEquals("https://x/agents", SwfteClient.deriveApiBaseUrl("https://x/agents/gateway/"));
            assertEquals("https://x/gateways", SwfteClient.deriveApiBaseUrl("https://x/gateways"));
        }
    }
}
