package com.swfte.sdk.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import com.swfte.sdk.HttpClient;
import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.exceptions.SwfteException;
import com.swfte.sdk.models.ChatChunk;
import com.swfte.sdk.models.ChatRequest;
import com.swfte.sdk.models.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/** Real consumed bodies, not simulated connect failures or request-count mocks. */
@Timeout(30)
class PostHelperReplayTest {
    private static final String TEXT = "hello caf\u00e9";
    private static final byte[] AUDIO = new byte[] { 0, (byte) 255, 16, 97, 117, 100, 105, 111 };
    private static final ObjectMapper JSON = new ObjectMapper();
    private final List<Endpoint> endpoints = new ArrayList<>();

    private static final class Hit {
        final String method;
        final String path;
        final Headers headers;
        final byte[] body;
        Hit(String method, String path, Headers headers, byte[] body) {
            this.method = method;
            this.path = path;
            this.headers = headers;
            this.body = body;
        }
    }

    private static final class Endpoint {
        final HttpServer server;
        final ExecutorService executor = Executors.newCachedThreadPool();
        final List<Hit> hits = Collections.synchronizedList(new ArrayList<>());
        Endpoint(boolean disconnect, int status, byte[] reply, String contentType, String location) throws IOException {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.setExecutor(executor);
            server.createContext("/", ex -> {
                byte[] consumed = ex.getRequestBody().readAllBytes();
                Headers copy = new Headers();
                copy.putAll(ex.getRequestHeaders());
                hits.add(new Hit(ex.getRequestMethod(), ex.getRequestURI().getPath(), copy, consumed));
                if (disconnect) {
                    ex.close(); // Entire body consumed, but no response status/headers sent.
                    return;
                }
                try {
                    ex.getResponseHeaders().set("Content-Type", contentType);
                    if (location != null) ex.getResponseHeaders().set("Location", location);
                    ex.sendResponseHeaders(status, reply.length);
                    try (OutputStream out = ex.getResponseBody()) { out.write(reply); }
                } finally { ex.close(); }
            });
            server.start();
        }
        String url() { return "http://127.0.0.1:" + server.getAddress().getPort(); }
        void stop() { server.stop(0); executor.shutdownNow(); }
    }

    private Endpoint endpoint(boolean disconnect, int status, byte[] reply, String type, String location) throws IOException {
        assertNotEquals("false", System.getProperty("sun.net.http.retryPost", "true"),
            "driver must leave native buffered POST replay enabled to exercise the guard");
        Endpoint result = new Endpoint(disconnect, status, reply, type, location);
        endpoints.add(result);
        return result;
    }

    private Endpoint dropped() throws IOException { return endpoint(true, 200, new byte[0], "application/json", null); }
    private Endpoint normal(String body) throws IOException {
        return endpoint(false, 200, body.getBytes(StandardCharsets.UTF_8), "application/json", null);
    }
    private SwfteClient client(Endpoint e, int retries) {
        return SwfteClient.builder().apiKey("post-replay-test-key").workspaceId("ws-post")
            .baseUrl(e.url() + "/v2/gateway").apiBaseUrl(e.url()).timeout(2000).maxRetries(retries).build();
    }
    private static ChatRequest chat() {
        return ChatRequest.builder().model("m").messages(List.of(new Message("user", TEXT))).build();
    }
    private static List<ChatChunk> streamedChat(SwfteClient sdk) {
        try (Stream<ChatChunk> stream = sdk.chat().completions().createStream(chat())) {
            return stream.collect(Collectors.toList());
        }
    }
    private static Hit consumedOnce(Endpoint e) {
        assertEquals(1, e.hits.size(), "a fully consumed POST was replayed");
        Hit hit = e.hits.get(0);
        assertEquals("POST", hit.method);
        assertEquals("Bearer post-replay-test-key", hit.headers.getFirst("Authorization"));
        assertEquals("ws-post", hit.headers.getFirst("X-Workspace-ID"));
        assertTrue(hit.body.length > 0, "a complete body must reach the loopback server");
        if (hit.headers.getFirst("Content-Length") != null) {
            assertEquals(hit.body.length, Integer.parseInt(hit.headers.getFirst("Content-Length")));
        } else {
            assertEquals("chunked", hit.headers.getFirst("Transfer-Encoding"));
        }
        return hit;
    }
    private static void multipart(Hit hit) {
        String type = hit.headers.getFirst("Content-Type");
        assertTrue(type.startsWith("multipart/form-data; boundary="));
        String boundary = type.substring(type.indexOf("boundary=") + 9);
        String body = new String(hit.body, StandardCharsets.ISO_8859_1);
        assertTrue(body.startsWith("--" + boundary + "\r\n"));
        assertTrue(body.endsWith("--" + boundary + "--\r\n"));
        assertTrue(body.contains(new String(AUDIO, StandardCharsets.ISO_8859_1)), "binary part changed");
    }

    @AfterEach void stopServers() { for (Endpoint e : endpoints) e.stop(); }

    @Test void streamedChatNeverReplaysConsumedPostWhenResponseCloses() throws Exception {
        for (int retries : new int[] { 0, 3 }) {
            Endpoint e = dropped();
            assertThrows(SwfteException.class, () -> streamedChat(client(e, retries)));
            Hit hit = consumedOnce(e);
            assertEquals(TEXT, JSON.readTree(hit.body).get("messages").get(0).get("content").asText());
        }
    }

    @Test void streamedChatStillParsesSseAndPreservesUtf8Body() throws Exception {
        String chunk = "{\"id\":\"stream-1\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"ok\"}}]}";
        Endpoint e = endpoint(false, 200, ("event: chunk\ndata:" + chunk + "\n\ndata: [DONE]\n\n")
            .getBytes(StandardCharsets.UTF_8), "text/event-stream", null);
        List<ChatChunk> result = streamedChat(client(e, 3));
        assertEquals(1, result.size());
        assertEquals("ok", result.get(0).getChoices().get(0).getDelta().getContent());
        assertEquals(TEXT, JSON.readTree(consumedOnce(e).body).get("messages").get(0).get("content").asText());
    }

    @Test void multipartHelpersNeverReplayConsumedPostWhenResponseCloses() throws Exception {
        for (int retries : new int[] { 0, 3 }) {
            for (int kind = 0; kind < 3; kind++) {
                Endpoint e = dropped();
                final int operation = kind;
                assertThrows(SwfteException.class, () -> {
                    SwfteClient sdk = client(e, retries);
                    if (operation == 0) sdk.audio().transcriptions().create("m", AUDIO, "fr", TEXT);
                    else if (operation == 1) sdk.files().upload("fixture.bin", "application/octet-stream", AUDIO);
                    else sdk.files().uploadBatch(Map.of("file", AUDIO, "name", "fixture.bin"));
                });
                multipart(consumedOnce(e));
            }
        }
    }

    @Test void transcriptionStillSendsMultipartFieldsAndReadsJson() throws Exception {
        Endpoint e = normal("{\"text\":\"ok\"}");
        assertEquals("ok", client(e, 3).audio().transcriptions().create("m", AUDIO, "fr", TEXT).getText());
        Hit hit = consumedOnce(e);
        multipart(hit);
        String text = new String(hit.body, StandardCharsets.UTF_8);
        assertTrue(text.contains("name=\"language\"\r\n\r\nfr\r\n"));
        assertTrue(text.contains("name=\"prompt\"\r\n\r\n" + TEXT + "\r\n"));
    }

    @Test void singleFileUploadStillSendsMultipartAndReadsMetadata() throws Exception {
        Endpoint e = normal("{\"id\":\"file-1\",\"name\":\"fixture.bin\",\"mimeType\":\"application/octet-stream\"}");
        assertEquals("file-1", client(e, 3).files().upload("fixture.bin", "application/octet-stream", AUDIO).getId());
        Hit hit = consumedOnce(e);
        multipart(hit);
        assertTrue(new String(hit.body, StandardCharsets.UTF_8).contains("fixture.bin"));
    }

    @Test void batchFileUploadStillSendsMultipartAndReadsJson() throws Exception {
        Endpoint e = normal("{\"count\":1}");
        assertEquals(1, client(e, 3).files().uploadBatch(Map.of("file", AUDIO, "name", "fixture.bin")).get("count"));
        multipart(consumedOnce(e));
    }

    @Test void fileUploadsUseOnlyApiBaseWithSuccessAndConsumedBodyDisconnect() throws Exception {
        for (boolean disconnect : new boolean[] { false, true }) {
            for (boolean batch : new boolean[] { false, true }) {
                Endpoint gateway = normal("{\"id\":\"wrong-gateway\",\"count\":99}");
                Endpoint api = disconnect ? dropped() : normal("{\"id\":\"file-api\",\"count\":1}");
                SwfteClient sdk = SwfteClient.builder().apiKey("post-replay-test-key").workspaceId("ws-post")
                    .baseUrl(gateway.url() + "/v2/gateway").apiBaseUrl(api.url()).timeout(2000).maxRetries(3).build();
                Object result = null;
                SwfteException refusal = null;
                try {
                    result = batch ? sdk.files().uploadBatch(Map.of("file", AUDIO, "name", "fixture.bin"))
                        : sdk.files().upload("fixture.bin", "application/octet-stream", AUDIO);
                } catch (SwfteException failed) { refusal = failed; }
                assertEquals(0, gateway.hits.size(), "file upload body or Authorization reached the gateway instead of apiBaseUrl");
                Hit hit = consumedOnce(api);
                multipart(hit);
                assertEquals(batch ? "/api/v2/files/upload-batch" : "/api/v2/files/upload", hit.path);
                if (disconnect) assertNotNull(refusal, "consumed file upload must fail without replaying");
                else {
                    assertNull(refusal);
                    if (batch) assertEquals(1, ((Map<?, ?>) result).get("count"));
                    else assertEquals("file-api", ((com.swfte.sdk.models.FileMetadata) result).getId());
                }
            }
        }
    }

    @Test void speechHelpersNeverReplayConsumedPostWhenResponseCloses() throws Exception {
        for (int retries : new int[] { 0, 3 }) {
            for (boolean extended : new boolean[] { false, true }) {
                Endpoint e = dropped();
                assertThrows(SwfteException.class, () -> {
                    if (extended) client(e, retries).audio().speech().create("m", TEXT, "voice", "wav", 1.2);
                    else client(e, retries).audio().speech().create("m", TEXT, "voice");
                });
                assertEquals(TEXT, JSON.readTree(consumedOnce(e).body).get("input").asText());
            }
        }
    }

    @Test void speechStillReturnsExactBinaryBytes() throws Exception {
        Endpoint e = endpoint(false, 200, AUDIO, "audio/mpeg", null);
        assertArrayEquals(AUDIO, client(e, 3).audio().speech().create("m", TEXT, "voice", "wav", 1.2));
        assertEquals(TEXT, JSON.readTree(consumedOnce(e).body).get("input").asText());
    }

    private static Object jsonCall(SwfteClient sdk, int kind) {
        HttpClient http = new HttpClient(sdk);
        Map<String, String> body = Map.of("prompt", TEXT);
        if (kind == 0) return http.post("/fixture", body, Map.class);
        if (kind == 1) return http.postWithCustomBase("/v2/fixture", body, Map.class);
        return http.apiRequest("POST", "/v2/fixture", body, Map.class);
    }

    private static Object emptyCall(SwfteClient sdk, int kind) {
        HttpClient http = new HttpClient(sdk);
        if (kind == 0) return http.post("/fixture", null, Map.class);
        if (kind == 1) return http.postWithCustomBase("/v2/fixture", null, Map.class);
        return http.apiRequest("POST", "/v2/fixture", null, Map.class);
    }

    private static void consumedEmptyOnce(Endpoint e) {
        assertEquals(1, e.hits.size(), "an empty side-effecting POST was replayed");
        Hit hit = e.hits.get(0);
        assertEquals("POST", hit.method);
        assertEquals("Bearer post-replay-test-key", hit.headers.getFirst("Authorization"));
        assertEquals("ws-post", hit.headers.getFirst("X-Workspace-ID"));
        assertEquals(0, hit.body.length);
        assertEquals("0", hit.headers.getFirst("Content-Length"));
    }

    @Test void centralEmptyPostsNeverReplayConsumedRequests() throws Exception {
        for (int retries : new int[] { 0, 3 }) {
            for (int kind = 0; kind < 3; kind++) {
                Endpoint e = dropped();
                final int operation = kind;
                assertThrows(SwfteException.class, () -> emptyCall(client(e, retries), operation));
                consumedEmptyOnce(e);
            }
        }
    }

    @Test void centralEmptyPostsStillReturnNormalResponses() throws Exception {
        for (int kind = 0; kind < 3; kind++) {
            Endpoint e = normal("{\"ok\":true}");
            assertEquals(Map.of("ok", true), emptyCall(client(e, 3), kind));
            consumedEmptyOnce(e);
        }
    }

    @Test void centralEmptyPostsNeverFollowCrossOriginRedirects() throws Exception {
        for (int status : new int[] { 302, 307 }) {
            for (int kind = 0; kind < 3; kind++) {
                Endpoint sink = normal("{\"ok\":true}");
                Endpoint source = endpoint(false, status, "{}".getBytes(StandardCharsets.UTF_8),
                    "application/json", sink.url() + "/sink");
                SwfteException refusal = null;
                try { emptyCall(client(source, 3), kind); }
                catch (SwfteException refused) { refusal = refused; }
                assertEquals(0, sink.hits.size(), "empty POST redirect sent a request or credentials across origins");
                consumedEmptyOnce(source);
                assertNotNull(refusal, "empty POST redirect must retain an error");
            }
        }
    }

    @Test void centralJsonPostsNeverReplayConsumedBody() throws Exception {
        for (int retries : new int[] { 0, 3 }) {
            for (int kind = 0; kind < 3; kind++) {
                Endpoint e = dropped();
                final int operation = kind;
                assertThrows(SwfteException.class, () -> jsonCall(client(e, retries), operation));
                assertEquals(TEXT, JSON.readTree(consumedOnce(e).body).get("prompt").asText());
            }
        }
    }

    @Test void centralJsonPostsStillReturnNormalResponses() throws Exception {
        for (int kind = 0; kind < 3; kind++) {
            Endpoint e = normal("{\"ok\":true}");
            assertEquals(Map.of("ok", true), jsonCall(client(e, 3), kind));
            consumedOnce(e);
        }
    }

    @Test void specialPostHelpersNeverDiscloseBodiesOrHeadersAcrossRedirectOrigins() throws Exception {
        for (int status : new int[] { 302, 307 }) {
            for (int kind = 0; kind < 6; kind++) {
                Endpoint sink = normal("{\"text\":\"sink\",\"id\":\"sink\"}");
                Endpoint source = endpoint(false, status, "{}".getBytes(StandardCharsets.UTF_8), "application/json", sink.url() + "/sink");
                final int operation = kind;
                SwfteException refusal = null;
                try {
                    SwfteClient sdk = client(source, 3);
                    if (operation == 0) streamedChat(sdk);
                    else if (operation == 1) sdk.audio().transcriptions().create("m", AUDIO);
                    else if (operation == 2) sdk.files().upload("fixture.bin", "application/octet-stream", AUDIO);
                    else if (operation == 3) sdk.files().uploadBatch(Map.of("file", AUDIO));
                    else if (operation == 4) sdk.audio().speech().create("m", TEXT, "voice");
                    else sdk.audio().speech().create("m", TEXT, "voice", "wav", 1.2);
                } catch (SwfteException refused) {
                    refusal = refused;
                }
                assertEquals(0, sink.hits.size(), "redirect disclosed request method, body or credential headers");
                consumedOnce(source);
                assertNotNull(refusal, "a redirect must not be treated as a successful result");
            }
        }
    }

    @Test void centralPostsNeverDiscloseBodiesOrHeadersAcrossRedirectOrigins() throws Exception {
        for (int status : new int[] { 302, 307 }) {
            for (int kind = 0; kind < 3; kind++) {
                Endpoint sink = normal("{\"ok\":true}");
                Endpoint source = endpoint(false, status, "{}".getBytes(StandardCharsets.UTF_8), "application/json", sink.url() + "/sink");
                final int operation = kind;
                assertThrows(SwfteException.class, () -> jsonCall(client(source, 3), operation));
                assertEquals(0, sink.hits.size(), "central POST disclosed body or credentials across origins");
                consumedOnce(source);
            }
        }
    }
}
