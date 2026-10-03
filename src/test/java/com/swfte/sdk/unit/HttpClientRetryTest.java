package com.swfte.sdk.unit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.swfte.sdk.HttpClient;
import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.exceptions.ApiException;
import com.swfte.sdk.exceptions.AuthenticationException;
import com.swfte.sdk.exceptions.RateLimitException;
import com.swfte.sdk.exceptions.SwfteException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Retry policy of {@link HttpClient}: a request that may already have run is never repeated,
 * and a 4xx is never retried. Every test counts the requests a real local server received.
 */
class HttpClientRetryTest {

    /** status/body/headers per call, in order; the last entry repeats. */
    private static final class Reply {
        final int status;
        final String body;
        final String retryAfter;
        final long delayMs;

        Reply(int status, String body, String retryAfter, long delayMs) {
            this.status = status;
            this.body = body;
            this.retryAfter = retryAfter;
            this.delayMs = delayMs;
        }
    }

    private static Reply reply(int status, String body) {
        return new Reply(status, body, null, 0);
    }

    private HttpServer server;
    private final List<String> hits = Collections.synchronizedList(new ArrayList<>());
    private final List<String> idempotencyKeys = Collections.synchronizedList(new ArrayList<>());

    @AfterEach
    void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private HttpClient start(int maxRetries, int timeoutMs, Reply... replies) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.setExecutor(Executors.newCachedThreadPool());
        server.createContext("/", (HttpExchange ex) -> {
            exchange(ex, replies);
        });
        server.start();
        String base = "http://127.0.0.1:" + server.getAddress().getPort();
        SwfteClient client = SwfteClient.builder()
            .apiKey("sk-swfte-test")
            .baseUrl(base + "/v2/gateway")
            .apiBaseUrl(base)
            .maxRetries(maxRetries)
            .timeout(timeoutMs)
            .build();
        return new HttpClient(client);
    }

    private void exchange(HttpExchange ex, Reply[] replies) throws IOException {
        int n;
        synchronized (hits) {
            hits.add(ex.getRequestMethod() + " " + ex.getRequestURI().getPath());
            n = hits.size();
        }
        idempotencyKeys.add(String.valueOf(ex.getRequestHeaders().getFirst("Idempotency-Key")));
        ex.getRequestBody().readAllBytes();
        Reply r = replies[Math.min(n, replies.length) - 1];
        if (r.delayMs > 0) {
            try {
                Thread.sleep(r.delayMs);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
        byte[] payload = r.body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json");
        if (r.retryAfter != null) {
            ex.getResponseHeaders().add("Retry-After", r.retryAfter);
        }
        try {
            ex.sendResponseHeaders(r.status, payload.length);
            try (OutputStream out = ex.getResponseBody()) {
                out.write(payload);
            }
        } catch (IOException clientGone) {
            // the client timed out and closed the socket
        }
    }

    // ---- 4xx: exactly one request, typed exception -------------------------------------------

    @Test
    void status403OnPostIsAuthenticationExceptionAfterExactlyOneRequest() throws Exception {
        HttpClient http = start(3, 5000, reply(403, "{\"error\":\"forbidden\"}"));
        assertThrows(AuthenticationException.class,
            () -> http.postWithCustomBase("/v2/things", Map.of("a", 1), Map.class));
        assertEquals(1, hits.size());
    }

    @Test
    void status403OnGatewayGetIsAuthenticationExceptionAfterExactlyOneRequest() throws Exception {
        HttpClient http = start(3, 5000, reply(403, "{}"));
        assertThrows(AuthenticationException.class, () -> http.get("/models", Map.class));
        assertEquals(1, hits.size());
    }

    @Test
    void status401IsAuthenticationExceptionAfterExactlyOneRequest() throws Exception {
        HttpClient http = start(3, 5000, reply(401, "{}"));
        assertThrows(AuthenticationException.class, () -> http.getWithCustomBase("/v2/things", Map.class));
        assertEquals(1, hits.size());
    }

    @Test
    void status404OnGetIsApiExceptionWithStatusAfterExactlyOneRequest() throws Exception {
        HttpClient http = start(3, 5000, reply(404, "{\"error\":\"nope\"}"));
        ApiException e = assertThrows(ApiException.class, () -> http.getWithCustomBase("/v2/things/x", Map.class));
        assertEquals(404, e.getStatusCode());
        assertTrue(e.getResponseBody().contains("nope"));
        assertEquals(1, hits.size());
    }

    @Test
    void status400OnPostIsApiExceptionAfterExactlyOneRequest() throws Exception {
        HttpClient http = start(3, 5000, reply(400, "{\"error\":\"bad\"}"));
        ApiException e = assertThrows(ApiException.class,
            () -> http.post("/chat/completions", Map.of("a", 1), Map.class));
        assertEquals(400, e.getStatusCode());
        assertEquals(1, hits.size());
    }

    @Test
    void status429IsRateLimitExceptionCarryingRetryAfterAndIsNotRetried() throws Exception {
        HttpClient http = start(3, 5000, new Reply(429, "{}", "7", 0));
        RateLimitException e = assertThrows(RateLimitException.class,
            () -> http.getWithCustomBase("/v2/things", Map.class));
        assertEquals(Long.valueOf(7), e.getRetryAfterSeconds());
        assertEquals(1, hits.size());
    }

    @Test
    void status429WithoutRetryAfterHasNullRetryAfter() throws Exception {
        HttpClient http = start(3, 5000, reply(429, "{}"));
        RateLimitException e = assertThrows(RateLimitException.class,
            () -> http.getWithCustomBase("/v2/things", Map.class));
        assertNull(e.getRetryAfterSeconds());
    }

    // ---- writes are not repeated -------------------------------------------------------------

    @Test
    void postAfter500IsSentExactlyOnce() throws Exception {
        HttpClient http = start(3, 5000, reply(500, "{\"error\":\"boom\"}"));
        ApiException e = assertThrows(ApiException.class,
            () -> http.postWithCustomBase("/v2/things", Map.of("a", 1), Map.class));
        assertEquals(500, e.getStatusCode());
        assertEquals(1, hits.size());
    }

    @Test
    void gatewayPostAfter500IsSentExactlyOnce() throws Exception {
        HttpClient http = start(3, 5000, reply(500, "{}"));
        assertThrows(ApiException.class, () -> http.post("/chat/completions", Map.of("a", 1), Map.class));
        assertEquals(1, hits.size());
    }

    @Test
    void postReadTimeoutIsNotRetried() throws Exception {
        HttpClient http = start(3, 300, new Reply(200, "{}", null, 1500));
        assertThrows(SwfteException.class, () -> http.postWithCustomBase("/v2/things", Map.of("a", 1), Map.class));
        Thread.sleep(800);
        assertEquals(1, hits.size());
    }

    @Test
    void patchAfter500IsSentExactlyOnce() throws Exception {
        HttpClient http = start(3, 5000, reply(500, "{}"));
        assertThrows(ApiException.class, () -> http.patchWithCustomBase("/v2/things/1", Map.of("a", 1), Map.class));
        assertEquals(1, hits.size());
    }

    // ---- idempotent reads and keyed writes are retried ---------------------------------------

    @Test
    void getAfter500IsRetriedAndSucceeds() throws Exception {
        HttpClient http = start(3, 5000, reply(500, "{}"), reply(503, "{}"), reply(200, "{\"ok\":true}"));
        Map<?, ?> result = http.getWithCustomBase("/v2/things", Map.class);
        assertEquals(Boolean.TRUE, result.get("ok"));
        assertEquals(3, hits.size());
    }

    @Test
    void getThatKeepsFailingWith500IsAttemptedMaxRetriesTimesThenApiException() throws Exception {
        HttpClient http = start(3, 5000, reply(500, "{\"error\":\"down\"}"));
        ApiException e = assertThrows(ApiException.class, () -> http.getWithCustomBase("/v2/things", Map.class));
        assertEquals(500, e.getStatusCode());
        assertEquals(3, hits.size());
    }

    @Test
    void gatewayGetAfter500IsRetried() throws Exception {
        HttpClient http = start(3, 5000, reply(500, "{}"), reply(200, "{\"ok\":true}"));
        assertEquals(Boolean.TRUE, http.get("/models", Map.class).get("ok"));
        assertEquals(2, hits.size());
    }

    @Test
    void postWithIdempotencyKeyIsRetriedAfter500AndSendsTheKeyEveryTime() throws Exception {
        HttpClient http = start(3, 5000, reply(500, "{}"), reply(200, "{\"id\":\"1\"}"));
        Map<?, ?> result = http.requestWithCustomBase("POST", "/v2/things", Map.of("a", 1), Map.class, "key-123");
        assertEquals("1", result.get("id"));
        assertEquals(2, hits.size());
        assertEquals(List.of("key-123", "key-123"), idempotencyKeys);
    }

    @Test
    void postWithIdempotencyKeyIsStillNotRetriedAfter403() throws Exception {
        HttpClient http = start(3, 5000, reply(403, "{}"));
        assertThrows(AuthenticationException.class,
            () -> http.requestWithCustomBase("POST", "/v2/things", Map.of("a", 1), Map.class, "key-123"));
        assertEquals(1, hits.size());
    }

    @Test
    void getReadTimeoutIsRetried() throws Exception {
        HttpClient http = start(2, 300, new Reply(200, "{}", null, 1500), reply(200, "{\"ok\":true}"));
        assertEquals(Boolean.TRUE, http.getWithCustomBase("/v2/things", Map.class).get("ok"));
        assertEquals(2, hits.size());
    }

    // ---- misc contract -----------------------------------------------------------------------

    @Test
    void maxRetriesZeroStillSendsOneRequest() throws Exception {
        HttpClient http = start(0, 5000, reply(200, "{\"ok\":true}"));
        assertEquals(Boolean.TRUE, http.getWithCustomBase("/v2/things", Map.class).get("ok"));
        assertEquals(1, hits.size());
    }

    @Test
    void stringResponsesKeepTheirLineBreaks() throws Exception {
        HttpClient http = start(1, 5000, reply(200, "a,b\n1,2\n"));
        assertEquals("a,b\n1,2\n", http.getWithCustomBase("/v2/export", String.class));
    }

    @Test
    void userAgentCarriesThePomVersion() {
        assertNotEquals("unknown", com.swfte.sdk.SdkVersion.VERSION);
        assertFalse(com.swfte.sdk.SdkVersion.VERSION.contains("$"));
    }
}
