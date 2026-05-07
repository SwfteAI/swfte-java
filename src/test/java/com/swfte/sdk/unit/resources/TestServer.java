package com.swfte.sdk.unit.resources;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Tiny in-process HTTP server used by unit tests to assert that resource
 * classes hit the right URL with the right method, and to return a canned
 * JSON body. Avoids pulling in MockWebServer/OkHttp.
 */
public final class TestServer implements AutoCloseable {

    public static final class Recorded {
        public final String method;
        public final String path;
        public final String body;
        public final String authorization;
        public final String workspaceId;

        Recorded(String method, String path, String body, String authorization, String workspaceId) {
            this.method = method;
            this.path = path;
            this.body = body;
            this.authorization = authorization;
            this.workspaceId = workspaceId;
        }
    }

    private final HttpServer server;
    private final List<Recorded> recorded = new ArrayList<>();
    private final int port;

    public TestServer(String responseJson) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        this.port = server.getAddress().getPort();
        server.createContext("/", new Handler(responseJson, recorded));
        server.start();
    }

    public String baseUrl() {
        return "http://127.0.0.1:" + port;
    }

    public List<Recorded> recorded() {
        return recorded;
    }

    public Recorded last() {
        return recorded.isEmpty() ? null : recorded.get(recorded.size() - 1);
    }

    @Override
    public void close() {
        server.stop(0);
    }

    private static final class Handler implements HttpHandler {
        private final String responseJson;
        private final List<Recorded> recorded;

        Handler(String responseJson, List<Recorded> recorded) {
            this.responseJson = responseJson == null ? "{}" : responseJson;
            this.recorded = recorded;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getRawPath();
            String query = exchange.getRequestURI().getRawQuery();
            if (query != null && !query.isEmpty()) {
                path = path + "?" + query;
            }
            String overrideMethod = exchange.getRequestHeaders().getFirst("X-HTTP-Method-Override");
            if (overrideMethod != null && !overrideMethod.isEmpty()) {
                method = overrideMethod;
            }
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String auth = exchange.getRequestHeaders().getFirst("Authorization");
            String workspace = exchange.getRequestHeaders().getFirst("X-Workspace-ID");
            recorded.add(new Recorded(method, path, body, auth, workspace));

            byte[] payload = responseJson.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, payload.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(payload);
            }
        }
    }
}
