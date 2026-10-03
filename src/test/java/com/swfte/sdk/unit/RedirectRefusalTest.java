package com.swfte.sdk.unit;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import com.swfte.sdk.HttpClient;
import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.exceptions.ApiException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * No method, base, or helper may follow a redirect: the bearer key and workspace
 * header would be replayed at the Location. Mirrors swfte-node ({@code redirect: 'manual'}).
 */
@Timeout(30)
class RedirectRefusalTest {
    private static final String KEY = "redirect-refusal-test-key";
    private final List<Server> servers = new ArrayList<>();

    private static final class Hit {
        final String method;
        final String path;
        final Headers headers;
        Hit(String method, String path, Headers headers) {
            this.method = method;
            this.path = path;
            this.headers = headers;
        }
    }

    private static final class Server {
        final HttpServer server;
        final ExecutorService executor = Executors.newCachedThreadPool();
        final List<Hit> hits = Collections.synchronizedList(new ArrayList<>());
        volatile String location;
        volatile int status;

        Server(int status, String location) throws IOException {
            this.status = status;
            this.location = location;
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.setExecutor(executor);
            server.createContext("/", ex -> {
                ex.getRequestBody().readAllBytes();
                Headers copy = new Headers();
                copy.putAll(ex.getRequestHeaders());
                hits.add(new Hit(ex.getRequestMethod(), ex.getRequestURI().getPath(), copy));
                byte[] reply = "{\"ok\":true}".getBytes(StandardCharsets.UTF_8);
                try {
                    ex.getResponseHeaders().set("Content-Type", "application/json");
                    if (this.location != null) ex.getResponseHeaders().set("Location", this.location);
                    ex.sendResponseHeaders(this.status, reply.length);
                    try (OutputStream out = ex.getResponseBody()) { out.write(reply); }
                } finally { ex.close(); }
            });
            server.start();
        }
        int port() { return server.getAddress().getPort(); }
        String url() { return "http://127.0.0.1:" + port(); }
        // A different host name for the same loopback listener: a different origin to any client.
        String otherHostUrl() { return "http://localhost:" + port(); }
        void stop() { server.stop(0); executor.shutdownNow(); }
    }

    private Server server(int status, String location) throws IOException {
        Server s = new Server(status, location);
        servers.add(s);
        return s;
    }

    private static SwfteClient client(Server source, int retries) {
        return SwfteClient.builder().apiKey(KEY).workspaceId("ws-redirect")
            .baseUrl(source.url() + "/v2/gateway").apiBaseUrl(source.url()).timeout(2000).maxRetries(retries).build();
    }

    @AfterEach void stopServers() { for (Server s : servers) s.stop(); }

    /** One request on the gateway or API base, by every transport path the SDK has. */
    private static final Map<String, Function<HttpClient, Object>> OPERATIONS = Map.of(
        "GET gateway", http -> http.get("/models", String.class),
        "GET custom base", http -> http.requestWithCustomBase("GET", "/v2/things", null, String.class),
        "GET apiRequest", http -> http.apiRequest("GET", "/v2/things", null, String.class),
        "DELETE gateway", http -> http.request("DELETE", "/models/x", null, String.class),
        "PUT gateway", http -> http.request("PUT", "/models/x", Map.of("a", 1), String.class),
        "PATCH gateway", http -> http.request("PATCH", "/models/x", Map.of("a", 1), String.class),
        "POST gateway", http -> http.post("/models", Map.of("a", 1), String.class),
        "POST bytes", http -> http.postBytes("/audio", Map.of("a", 1)),
        "POST stream", http -> http.postStream("/chat", Map.of("a", 1)).count(),
        "POST multipart", http -> http.postMultipart("/files", Map.of("name", "n"), Map.class));

    private static void assertRefused(Server source, Server sink, int status, String what, ApiException refusal) {
        assertNotNull(refusal, what + ": a " + status + " must be an error, not a success");
        assertEquals(status, refusal.getStatusCode(), what);
        assertTrue(refusal.getMessage().contains("Refusing to follow a redirect (" + status + ")"), refusal.getMessage());
        assertFalse(refusal.getMessage().contains(KEY), "the key must never appear in the refusal");
        assertEquals(1, source.hits.size(), what + ": the redirect was followed or retried");
        assertEquals(0, sink.hits.size(), what + ": the request (and its credentials) reached the redirect target");
    }

    private static ApiException run(Function<HttpClient, Object> operation, HttpClient http) {
        try {
            operation.apply(http);
            return null;
        } catch (ApiException refused) {
            return refused;
        }
    }

    @Test void everyMethodRefusesSameHostRedirects() throws Exception {
        for (int status : new int[] { 301, 302, 307 }) {
            for (Map.Entry<String, Function<HttpClient, Object>> op : OPERATIONS.entrySet()) {
                Server sink = server(200, null);
                Server source = server(status, null);
                source.location = source.url() + "/elsewhere";
                // maxRetries 3: GET would be retried on a failure, but a refused redirect is final.
                ApiException refusal = run(op.getValue(), new HttpClient(client(source, 3)));
                assertRefused(source, sink, status, op.getKey() + " same-host " + status, refusal);
                assertNotEquals("/elsewhere", source.hits.get(0).path);
            }
        }
    }

    @Test void everyMethodRefusesCrossHostRedirectsAndNeverSendsAuthorization() throws Exception {
        for (int status : new int[] { 301, 302, 307 }) {
            for (Map.Entry<String, Function<HttpClient, Object>> op : OPERATIONS.entrySet()) {
                Server sink = server(200, null);
                Server source = server(status, sink.otherHostUrl() + "/sink");
                ApiException refusal = run(op.getValue(), new HttpClient(client(source, 3)));
                assertRefused(source, sink, status, op.getKey() + " cross-host " + status, refusal);
                for (Hit hit : sink.hits) {
                    assertNull(hit.headers.getFirst("Authorization"), "Authorization reached the redirect target");
                    assertNull(hit.headers.getFirst("X-Workspace-ID"), "workspace header reached the redirect target");
                }
            }
        }
    }

    @Test void aSuccessfulGetIsStillReturned() throws Exception {
        Server source = server(200, null);
        assertEquals("{\"ok\":true}", new HttpClient(client(source, 3)).get("/models", String.class));
        assertEquals(1, source.hits.size());
        assertEquals("Bearer " + KEY, source.hits.get(0).headers.getFirst("Authorization"));
    }

    /**
     * Positive control for the harness itself: a stock HttpURLConnection that does follow
     * redirects reaches the sink and replays custom headers (the JDK drops Authorization on
     * a cross-origin hop but keeps it on a same-origin hop and never drops X-Workspace-ID), so the assertions above can see a disclosure.
     */
    @Test void controlADefaultConnectionDoesFollowAndReplayHeaders() throws Exception {
        Server sink = server(200, null);
        Server crossHost = server(302, sink.otherHostUrl() + "/sink");
        HttpURLConnection conn = (HttpURLConnection) new URL(crossHost.url() + "/models").openConnection();
        conn.setRequestProperty("Authorization", "Bearer " + KEY);
        conn.setRequestProperty("X-Workspace-ID", "ws-redirect");
        assertEquals(200, conn.getResponseCode());
        assertEquals(1, sink.hits.size(), "control: the default connection must reach the cross-host sink");
        assertEquals("ws-redirect", sink.hits.get(0).headers.getFirst("X-Workspace-ID"));

        // Same origin (same host and port): the JDK replays Authorization on the second hop.
        Server loop = server(302, null);
        loop.location = loop.url() + "/again";
        HttpURLConnection same = (HttpURLConnection) new URL(loop.url() + "/models").openConnection();
        same.setRequestProperty("Authorization", "Bearer " + KEY);
        try { same.getResponseCode(); } catch (IOException tooMany) { /* the server redirects forever */ }
        assertTrue(loop.hits.size() > 1, "control: the default connection must follow the same-origin redirect");
        assertEquals("Bearer " + KEY, loop.hits.get(1).headers.getFirst("Authorization"));
    }
}
