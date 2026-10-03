package com.swfte.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swfte.sdk.exceptions.ApiException;
import com.swfte.sdk.exceptions.AuthenticationException;
import com.swfte.sdk.exceptions.RateLimitException;
import com.swfte.sdk.exceptions.SwfteException;
import com.swfte.sdk.exceptions.WorkflowExecutionException;
import com.swfte.sdk.exceptions.WorkflowPausedException;
import com.swfte.sdk.exceptions.WorkflowTimeoutException;
import com.swfte.sdk.models.ChatRequest;
import com.swfte.sdk.models.WorkflowExecution;
import com.swfte.sdk.unit.resources.TestServer;
import com.swfte.sdk.unit.resources.TestServer.Response;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CredentialPrivacyTest {
    private static final String KEY = "opaque-review-\"-\\-/-space fixture";
    private static final ObjectMapper JSON = new ObjectMapper();

    private static SwfteClient client(String base, String key) {
        return SwfteClient.builder().apiKey(key).baseUrl(base).apiBaseUrl(base).maxRetries(0).timeout(1000).build();
    }

    private static Map<String, Object> echo(String key) {
        return Map.of("message", "backend refused " + key, "safe", "retry in Studio", "nested", Map.of(key, List.of(key, 7, false)));
    }

    private static String json(Object value) throws Exception {
        return JSON.writeValueAsString(value);
    }

    private static void assertPrivate(Throwable error, String key) throws Exception {
        StringWriter trace = new StringWriter();
        error.printStackTrace(new PrintWriter(trace));
        String diagnostic = error + "\n" + trace + "\n" + json(error);
        assertFalse(diagnostic.contains(key), diagnostic);
        assertFalse(diagnostic.contains(JSON.writeValueAsString(key).substring(1, JSON.writeValueAsString(key).length() - 1)), diagnostic);
        assertFalse(diagnostic.contains(URLEncoder.encode(key, StandardCharsets.UTF_8)), diagnostic);
        assertFalse(diagnostic.contains(URLEncoder.encode(key, StandardCharsets.UTF_8).replace("+", "%20")), diagnostic);
    }

    @Test
    void httpTypedErrorsRedactNestedBodiesAndEncodedLabelsWithoutReplay() throws Exception {
        for (String key : new String[]{KEY, "r5X", "~"}) {
            for (int status : new int[]{401, 403, 429, 500}) {
                try (TestServer server = new TestServer(new Response(status, json(echo(key))))) {
                    HttpClient http = new HttpClient(client(server.baseUrl(), key));
                    SwfteException error = assertThrows(SwfteException.class, () -> http.apiRequest("POST",
                        "/fixture/" + URLEncoder.encode(key, StandardCharsets.UTF_8).replace("+", "%20"), Map.of(), Map.class));
                    if (status == 401 || status == 403) assertInstanceOf(AuthenticationException.class, error);
                    else if (status == 429) assertInstanceOf(RateLimitException.class, error);
                    else {
                        ApiException api = assertInstanceOf(ApiException.class, error);
                        assertEquals(status, api.getStatusCode());
                        Map<?, ?> body = JSON.readValue(api.getResponseBody(), Map.class);
                        assertEquals("retry in Studio", body.get("safe"));
                        assertEquals(List.of("[REDACTED]", 7, false), ((Map<?, ?>) body.get("nested")).get("[REDACTED]"));
                    }
                    assertTrue(error.getMessage().contains("API error: " + status), error.getMessage());
                    // Fixed-length JDK POST refuses an authentication replay and
                    // exposes no 401 error stream; other bodies retain safe detail.
                    if (status != 401) assertTrue(error.getMessage().contains("backend refused"), error.getMessage());
                    assertEquals(1, server.recorded().size());
                    assertEquals("Bearer " + key, server.last().authorization);
                    assertPrivate(error, key);
                }
            }
        }
    }

    @Test
    void plainTextAndGatewayErrorsKeepUsefulDetailAndTypedStatus() throws Exception {
        try (TestServer server = new TestServer(new Response(409, "safe plain detail " + KEY))) {
            ApiException error = assertThrows(ApiException.class, () -> new HttpClient(client(server.baseUrl(), KEY))
                .post("/fixture", Map.of(), Map.class));
            assertEquals(409, error.getStatusCode());
            assertTrue(error.getResponseBody().contains("safe plain detail"));
            assertPrivate(error, KEY);
            assertEquals(1, server.recorded().size());
        }
        StubConnection rate = new StubConnection(429, new ByteArrayInputStream(new byte[0]), json(echo(KEY)));
        RateLimitException error = assertThrows(RateLimitException.class, () -> new HttpClient(client("http://fixture.test", KEY), ignored -> rate)
            .apiRequest("POST", "/fixture", Map.of(), Map.class));
        assertEquals(Long.valueOf(7), error.getRetryAfterSeconds());
        assertPrivate(error, KEY);
    }

    @Test
    void encodedLowerHexAndDoubleEncodedPathLabelsArePrivate() throws Exception {
        String key = "fixt ur/e~re";
        for (String label : new String[]{"fixt%20ur%2Fe~re", "fixt%20ur/e~re", "fixt%20ur%2fe~re", "fixt%2520ur%252Fe~re"}) {
            try (TestServer server = new TestServer(new Response(409, "safe detail"))) {
                ApiException error = assertThrows(ApiException.class, () -> new HttpClient(client(server.baseUrl(), key))
                    .apiRequest("POST", "/fixture/" + label, Map.of(), Map.class));
                assertFalse(error.getMessage().contains(label), "the error path must not retain this encoded credential");
                assertEquals(409, error.getStatusCode());
                assertEquals("safe detail", error.getResponseBody());
                assertEquals(1, server.recorded().size());
            }
        }
    }

    @Test
    void ioAndParserFailuresDetachPrivateCausesAndLeaveOriginalUntouched() throws Exception {
        IOException source = new SocketTimeoutException("network detail " + KEY);
        source.initCause(new IOException("cause detail " + URLEncoder.encode(KEY, StandardCharsets.UTF_8)));
        AtomicInteger calls = new AtomicInteger();
        HttpClient broken = new HttpClient(client("http://fixture.test", KEY), ignored -> { calls.incrementAndGet(); throw source; });
        SwfteException failure = assertThrows(SwfteException.class, () -> broken.apiRequest("POST", "/fixture", Map.of(), Map.class));
        assertInstanceOf(SocketTimeoutException.class, failure.getCause());
        assertNotSame(source, failure.getCause());
        assertTrue(failure.getCause().getMessage().contains("network detail"));
        assertTrue(source.getMessage().contains(KEY));
        assertEquals(1, calls.get());
        assertPrivate(failure, KEY);
        try (TestServer server = new TestServer("{\"broken\":\"" + KEY)) {
            SwfteException parser = assertThrows(SwfteException.class, () -> new HttpClient(client(server.baseUrl(), KEY))
                .post("/fixture", Map.of(), Map.class));
            assertNotNull(parser.getCause());
            assertPrivate(parser, KEY);
        }
    }

    public static class FailingRequest {
        public String getValue() { throw new IllegalStateException("serialization detail " + KEY); }
    }

    @Test
    void serializationFailureIsPrivateAndMakesNoConnection() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        HttpClient http = new HttpClient(client("http://fixture.test", KEY), ignored -> { calls.incrementAndGet(); throw new IOException("should not connect"); });
        SwfteException error = assertThrows(SwfteException.class, () -> http.post("/fixture", new FailingRequest(), Map.class));
        assertTrue(error.getCause().getMessage().contains("serialization detail"));
        assertEquals(0, calls.get());
        assertPrivate(error, KEY);
    }

    @Test
    void lateStreamReaderFailureIsPrivateAndSuccessfulFrameStaysUntouched() throws Exception {
        String frame = json(Map.of("content", KEY));
        byte[] first = ("data: " + frame + "\n\n").getBytes(StandardCharsets.UTF_8);
        AtomicInteger closes = new AtomicInteger();
        InputStream input = new InputStream() {
            int offset;
            @Override public int read() throws IOException {
                if (offset < first.length) return first[offset++] & 0xff;
                throw new IOException("reader detail " + KEY);
            }
            @Override public void close() { closes.incrementAndGet(); }
        };
        StubConnection conn = new StubConnection(200, input, "");
        try (Stream<String> stream = new HttpClient(client("http://fixture.test", KEY), ignored -> conn).postStream("/fixture", Map.of())) {
            Iterator<String> frames = stream.iterator();
            assertEquals(frame, frames.next());
            SwfteException error = assertThrows(SwfteException.class, frames::hasNext);
            assertTrue(error.getCause().getMessage().contains("reader detail"));
            assertPrivate(error, KEY);
            assertEquals(1, closes.get());
            assertTrue(conn.disconnected);
        }
    }

    @Test
    void streamCloseFailureIsPrivateAndDisconnectStillHappens() throws Exception {
        InputStream input = new InputStream() {
            @Override public int read() { return -1; }
            @Override public void close() throws IOException { throw new IOException("close detail " + KEY); }
        };
        StubConnection conn = new StubConnection(200, input, "");
        Stream<String> stream = new HttpClient(client("http://fixture.test", KEY), ignored -> conn).postStream("/fixture", Map.of());
        SwfteException error = assertThrows(SwfteException.class, stream::close);
        assertTrue(error.getCause().getMessage().contains("close detail"));
        assertPrivate(error, KEY);
        assertTrue(conn.disconnected);
    }

    @Test
    void streamErrorFrameIsTypedPrivateAndSuccessfulChunkIsUntouched() throws Exception {
        try (TestServer server = new TestServer("data: " + json(Map.of("error", echo(KEY))) + "\n\n")) {
            SwfteClient client = client(server.baseUrl(), KEY);
            try (Stream<?> stream = client.chat().completions().createStream(ChatRequest.builder().model("fixture").messages(List.of()).build())) {
                ApiException error = assertThrows(ApiException.class, () -> stream.collect(Collectors.toList()));
                assertEquals(500, error.getStatusCode());
                assertEquals("retry in Studio", ((Map<?, ?>) JSON.readValue(error.getResponseBody(), Map.class).get("error")).get("safe"));
                assertPrivate(error, KEY);
                assertEquals(1, server.recorded().size());
            }
        }
        String chunk = json(Map.of("id", "fixture", "choices", List.of(Map.of("index", 0, "delta", Map.of("content", KEY)))));
        try (TestServer server = new TestServer("data: " + chunk + "\n\ndata: [DONE]\n\n")) {
            try (Stream<String> stream = new HttpClient(client(server.baseUrl(), KEY)).postStream("/fixture", Map.of())) {
                assertEquals(List.of(chunk), stream.collect(Collectors.toList()));
            }
        }
    }

    @Test
    void multipartAndByteResponseErrorsRedactWithoutReplayingWrites() throws Exception {
        for (int operation = 0; operation < 3; operation++) {
            try (TestServer server = new TestServer(new Response(500, json(echo(KEY))))) {
                HttpClient http = new HttpClient(client(server.baseUrl(), KEY));
                final int selected = operation;
                ApiException error = assertThrows(ApiException.class, () -> {
                    if (selected == 0) http.postMultipart("/fixture", Map.of("audio", new byte[]{1}), Map.class);
                    else if (selected == 1) http.postMultipartWithCustomBase("/fixture", Map.of("audio", new byte[]{1}), Map.class);
                    else http.postBytes("/fixture", Map.of());
                });
                assertEquals(500, error.getStatusCode());
                assertTrue(error.getResponseBody().contains("retry in Studio"));
                assertPrivate(error, KEY);
                assertEquals(1, server.recorded().size());
            }
        }
    }

    @Test
    void workflowMalformedAndTerminalFailuresRedactTheirStructuredExecution() throws Exception {
        try (TestServer server = new TestServer(new Response(202, json(echo(KEY))))) {
            ApiException error = assertThrows(ApiException.class, () -> client(server.baseUrl(), KEY).workflows().invoke("fixture", Map.of()));
            assertEquals(502, error.getStatusCode());
            assertTrue(error.getResponseBody().contains("retry in Studio"));
            assertPrivate(error, KEY);
        }
        for (String status : new String[]{"FAILED", "CANCELED", "PAUSED", "RUNNING"}) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("execution", Map.of("executionId", "execution-1", "status", status, "errorInfo", echo(KEY), "outputData", echo(KEY)));
            body.put("nodeExecutions", List.of(Map.of("nodeId", KEY, "nodeType", "HUMAN_INPUT", "status", "PAUSED", "pauseReason", KEY)));
            try (TestServer server = new TestServer(new Response(202, "{\"executionId\":\"execution-1\"}"), new Response(200, json(body)))) {
                SwfteException error = assertThrows(SwfteException.class, () -> client(server.baseUrl(), KEY).workflows().invokeAndWait("fixture", Map.of(), 0, 0, true));
                WorkflowExecution execution;
                if (status.equals("RUNNING")) execution = assertInstanceOf(WorkflowTimeoutException.class, error).getLastStatus();
                else if (status.equals("PAUSED")) {
                    WorkflowPausedException paused = assertInstanceOf(WorkflowPausedException.class, error);
                    assertEquals("[REDACTED]", paused.getWaitingFor().get(0).getNodeId());
                    assertEquals("[REDACTED]", paused.getWaitingFor().get(0).getReason());
                    execution = paused.getExecution();
                } else execution = assertInstanceOf(WorkflowExecutionException.class, error).getExecution();
                assertEquals(status, execution.getStatusRaw());
                assertEquals("execution-1", execution.getExecutionId());
                assertEquals("retry in Studio", execution.getOutputs().get("safe"));
                assertFalse(json(execution.getRaw()).contains(KEY));
                assertFalse(json(execution.getNodeExecutions()).contains(KEY));
                assertPrivate(error, KEY);
                assertEquals(2, server.recorded().size());
            }
        }
    }

    @Test
    void deploymentFailureRedactsBackendDetailAndCallerIdentifier() throws Exception {
        String key = "opaque-deployment-credential-fixture";
        try (TestServer server = new TestServer(json(Map.of("state", "FAILED", "statusMessage", "capacity detail " + key)))) {
            RuntimeException error = assertThrows(RuntimeException.class, () -> client(server.baseUrl(), key).deployments().waitForReady(key, 5000, 0));
            assertTrue(error.getMessage().contains("capacity detail"));
            assertPrivate(error, key);
        }
    }

    @Test
    void replacementMarkerNeverReintroducesLiteralCredentialValue() throws Exception {
        for (String key : new String[]{"E", "*"}) {
            try (TestServer server = new TestServer(new Response(500, json(Map.of("echo", key, "safe", "keep detail"))))) {
                ApiException error = assertThrows(ApiException.class, () -> new HttpClient(client(server.baseUrl(), key))
                    .apiRequest("POST", "/fixture", Map.of(), Map.class));
                Map<?, ?> body = JSON.readValue(error.getResponseBody(), Map.class);
                assertEquals(key.equals("E") ? "*" : "[REDACTED]", body.get("echo"));
                assertFalse(String.valueOf(body.get("echo")).contains(key));
                assertEquals("keep detail", body.get("safe"));
                assertEquals(500, error.getStatusCode());
            }
        }
    }

    @Test
    void successfulSecretPayloadAndExplicitCredentialAccessStayCompatible() throws Exception {
        Map<String, Object> value = Map.of("value", KEY, "nested", echo(KEY));
        try (TestServer server = new TestServer(json(value))) {
            SwfteClient client = client(server.baseUrl(), KEY);
            assertEquals(value, new HttpClient(client).apiRequest("GET", "/v2/secrets/fixture", null, Map.class));
            assertEquals(KEY, client.getApiKey());
            assertEquals("Bearer " + KEY, server.last().authorization);
        }
        try (TestServer server = new TestServer(KEY)) {
            assertArrayEquals(KEY.getBytes(StandardCharsets.UTF_8), new HttpClient(client(server.baseUrl(), KEY)).postBytes("/fixture", Map.of()));
        }
    }

    private static final class StubConnection extends HttpURLConnection {
        private final int status;
        private final InputStream input;
        private final String error;
        boolean disconnected;
        StubConnection(int status, InputStream input, String error) throws Exception {
            super(new URL("http://fixture.test"));
            this.status = status; this.input = input; this.error = error;
        }
        @Override public int getResponseCode() { return status; }
        @Override public InputStream getInputStream() { return input; }
        @Override public InputStream getErrorStream() { return new ByteArrayInputStream(error.getBytes(StandardCharsets.UTF_8)); }
        @Override public OutputStream getOutputStream() { return new ByteArrayOutputStream(); }
        @Override public String getHeaderField(String name) { return "Retry-After".equals(name) ? "7" : null; }
        @Override public void disconnect() { disconnected = true; }
        @Override public boolean usingProxy() { return false; }
        @Override public void connect() { }
    }
}
