package com.swfte.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

/** Real loopback transport controls; fixture success is not production endpoint evidence. */
class UncertainMutationTransportTest {
    private static final String ID = "cs_" + "a".repeat(24);
    private static final Map<String,Object> BODY = Map.of("text", "héllo 世界 🦊", "sessionId", "session-transport");
    private SwfteClient client(int port) {
        return SwfteClient.builder().apiKey("transport-test-token").workspaceId("workspace-A")
                .apiBaseUrl("http://127.0.0.1:" + port).maxRetries(3).timeout(2000)
                .callsiteResolver(new CallsiteResolver(name -> null, name -> null, null, message -> {})).build();
    }

    @Test void actualExecutePreservesUtf8HeadersSessionAndLegacySkipValidation() throws Exception {
        var accepted = new AtomicInteger();
        var observed = new java.util.concurrent.atomic.AtomicReference<Map<?,?>>();
        var failure = new java.util.concurrent.atomic.AtomicReference<Throwable>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            try {
                byte[] bytes = exchange.getRequestBody().readAllBytes();
                assertEquals(bytes.length, Integer.parseInt(exchange.getRequestHeaders().getFirst("Content-Length")));
                assertEquals("Bearer transport-test-token", exchange.getRequestHeaders().getFirst("Authorization"));
                assertEquals("workspace-A", exchange.getRequestHeaders().getFirst("X-Workspace-ID"));
                assertEquals(ID, exchange.getRequestHeaders().getFirst("X-Swfte-Callsite"));
                assertEquals("skipValidation=true", exchange.getRequestURI().getRawQuery());
                observed.set(new ObjectMapper().readValue(bytes, Map.class)); accepted.incrementAndGet();
                byte[] response = "{\"executionId\":\"execution-transport\",\"sessionId\":\"session-transport\",\"status\":\"PENDING\"}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length); exchange.getResponseBody().write(response);
            } catch (Throwable error) { failure.set(error); } finally { exchange.close(); }
        }); server.start();
        try {
            assertNotNull(client(server.getAddress().getPort()).workflows().execute("wf_transport", BODY, true, CallSite.of(ID)));
            if (failure.get() != null) throw new AssertionError("wire assertion failed", failure.get());
            assertEquals(1, accepted.get()); assertEquals(BODY, observed.get());
        } finally { server.stop(0); }
    }

    @Test void actualChatflowSessionPreservesContextAndCannotRepeatAcceptedSession() throws Exception {
        var effects = new AtomicInteger();
        var failure = new java.util.concurrent.atomic.AtomicReference<Throwable>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            try {
                byte[] bytes = exchange.getRequestBody().readAllBytes();
                assertEquals("POST", exchange.getRequestMethod());
                assertEquals("/v2/chatflows/cf_transport/sessions", exchange.getRequestURI().getPath());
                assertEquals(bytes.length, Integer.parseInt(exchange.getRequestHeaders().getFirst("Content-Length")));
                assertEquals("Bearer transport-test-token", exchange.getRequestHeaders().getFirst("Authorization"));
                assertEquals("workspace-A", exchange.getRequestHeaders().getFirst("X-Workspace-ID"));
                assertEquals(ID, exchange.getRequestHeaders().getFirst("X-Swfte-Callsite"));
                assertEquals(BODY, new ObjectMapper().readValue(bytes, Map.class));
                int count = effects.incrementAndGet();
                byte[] response = (count == 1 ? "{\"sessionId\":\"session-transport\",\"chatFlowId\":\"cf_transport\",\"workspaceId\":\"workspace-A\"}" : "accepted session but response failed").getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(count == 1 ? 200 : 500, response.length);
                exchange.getResponseBody().write(response);
            } catch (Throwable error) { failure.set(error); } finally { exchange.close(); }
        }); server.start();
        try {
            var sdk = client(server.getAddress().getPort());
            var session = sdk.chatflows().startSession("cf_transport", BODY, CallSite.of(ID));
            assertEquals("session-transport", session.getSessionId());
            assertEquals("workspace-A", session.getWorkspaceId());
            assertThrows(RuntimeException.class, () -> sdk.chatflows().startSession("cf_transport", BODY, CallSite.of(ID)));
            if (failure.get() != null) throw new AssertionError("session wire assertion failed", failure.get());
            assertEquals(2, effects.get()); // One successful session and exactly one uncertain accepted session.
        } finally { server.stop(0); }
    }

    @Test void acceptedMutationThen500HasExactlyOneEffectAtMaxRetriesThree() throws Exception {
        var effects = new AtomicInteger();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            exchange.getRequestBody().readAllBytes(); effects.incrementAndGet();
            byte[] body = "uncertain after acceptance".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(500, body.length); exchange.getResponseBody().write(body); exchange.close();
        }); server.start();
        try {
            assertThrows(RuntimeException.class, () -> client(server.getAddress().getPort()).workflows().execute("wf_transport", BODY, CallSite.of(ID)));
            assertEquals(1, effects.get()); // Removing maxAttempts=1 must repeat the accepted effect.
        } finally { server.stop(0); }
    }

    @Test void acceptedMutationThenSocketCloseCannotImplicitlyReplayPost() throws Exception {
        var effects = new AtomicInteger();
        try (ServerSocket server = new ServerSocket()) {
            server.bind(new InetSocketAddress("127.0.0.1", 0)); server.setSoTimeout(1500);
            ExecutorService worker = Executors.newSingleThreadExecutor();
            Future<?> listener = worker.submit(() -> {
                try {
                    while (true) try (Socket socket = server.accept()) {
                        socket.setSoTimeout(2000);
                        InputStream input = socket.getInputStream(); ByteArrayOutputStream header = new ByteArrayOutputStream();
                        int value; String text;
                        do {
                            value = input.read(); if (value < 0) throw new EOFException("request headers absent");
                            header.write(value); if (header.size() > 32768) throw new IOException("header cap");
                            text = header.toString(StandardCharsets.ISO_8859_1);
                        } while (!text.endsWith("\r\n\r\n"));
                        int size = -1;
                        for (String line : text.split("\r\n")) if (line.toLowerCase(java.util.Locale.ROOT).startsWith("content-length:")) size = Integer.parseInt(line.substring(line.indexOf(':') + 1).trim());
                        if (size < 0 || size > 32768) throw new IOException("fixed body length required");
                        if (input.readNBytes(size).length != size) throw new EOFException("request body not accepted");
                        effects.incrementAndGet(); // Accept all bytes, then close without any response.
                    }
                } catch (SocketTimeoutException done) { /* bounded observation of any implicit retry */ }
                catch (IOException error) { throw new UncheckedIOException(error); }
            });
            try {
                assertThrows(RuntimeException.class, () -> client(server.getLocalPort()).workflows().execute("wf_transport", BODY, CallSite.of(ID)));
                listener.get(6, TimeUnit.SECONDS);
                assertEquals(1, effects.get()); // Removing fixed-length mode can expose JDK POST replay.
            } finally { worker.shutdownNow(); }
        }
    }

    @Test void getStillRetries500Then200() throws Exception {
        var attempts = new AtomicInteger();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            int attempt = attempts.incrementAndGet(); byte[] body = (attempt == 1 ? "retry" : "{\"ok\":true}").getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(attempt == 1 ? 500 : 200, body.length); exchange.getResponseBody().write(body); exchange.close();
        }); server.start();
        try {
            Map<?,?> result = new HttpClient(client(server.getAddress().getPort())).requestWithCustomBase("GET", "/read", null, Map.class);
            assertEquals(Boolean.TRUE, result.get("ok")); assertEquals(2, attempts.get());
        } finally { server.stop(0); }
    }

    @Test void nullMutatingBodiesUseFixedZeroLengthAndNeverRetry() throws Exception {
        var effects = new java.util.concurrent.ConcurrentHashMap<String,AtomicInteger>();
        var failure = new java.util.concurrent.atomic.AtomicReference<Throwable>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            try {
                assertEquals("0", exchange.getRequestHeaders().getFirst("Content-Length"));
                assertEquals(0, exchange.getRequestBody().readAllBytes().length);
                effects.computeIfAbsent(exchange.getRequestURI().getPath(), key -> new AtomicInteger()).incrementAndGet();
                exchange.sendResponseHeaders(500, -1);
            } catch (Throwable error) { failure.set(error); } finally { exchange.close(); }
        }); server.start();
        try {
            var transport = new HttpClient(client(server.getAddress().getPort()));
            for (String method : java.util.List.of("POST", "PUT", "PATCH", "DELETE")) {
                assertThrows(RuntimeException.class, () -> transport.requestWithCustomBase(method, "/mutating/" + method, null, Void.class));
                if (failure.get() != null) throw new AssertionError("empty mutation wire assertion failed", failure.get());
                assertEquals(1, effects.get("/mutating/" + method).get());
            }
        } finally { server.stop(0); }
    }
}
