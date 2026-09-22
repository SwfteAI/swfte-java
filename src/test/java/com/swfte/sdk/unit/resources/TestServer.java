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

    /** A canned response: HTTP status + JSON body. */
    public static final class Response {
        public final int status;
        public final String body;

        public Response(int status, String body) {
            this.status = status;
            this.body = body;
        }
    }

    private final HttpServer server;
    private final List<Recorded> recorded = new ArrayList<>();
    private final int port;

    public TestServer(String responseJson) throws IOException {
        this(new Response(200, responseJson));
    }

    /**
     * Answer successive requests with {@code responses} in order; the last one
     * repeats once the list is exhausted.
     */
    public TestServer(Response... responses) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        this.port = server.getAddress().getPort();
        server.createContext("/", new Handler(responses, recorded));
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
        private final Response[] responses;
        private final List<Recorded> recorded;

        Handler(Response[] responses, List<Recorded> recorded) {
            this.responses = responses;
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
            Response response;
            synchronized (recorded) {
                recorded.add(new Recorded(method, path, body, auth, workspace));
                response = responses[Math.min(recorded.size(), responses.length) - 1];
            }

            String json = response.body == null ? "{}" : response.body;
            byte[] payload = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(response.status, payload.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(payload);
            }
        }
    }
}
