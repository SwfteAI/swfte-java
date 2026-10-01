package com.swfte.sdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.swfte.sdk.exceptions.AuthenticationException;
import com.swfte.sdk.exceptions.RateLimitException;
import com.swfte.sdk.exceptions.ApiException;
import com.swfte.sdk.exceptions.SwfteException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * HTTP client for making API requests.
 */
public class HttpClient {
    
    private final SwfteClient client;
    private final ObjectMapper objectMapper;
    
    public HttpClient(SwfteClient client) {
        this.client = client;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    
    /**
     * Make a POST request.
     */
    public <T> T post(String path, Object body, Class<T> responseType) {
        return request("POST", path, body, responseType);
    }
    
    /**
     * Make a GET request.
     */
    public <T> T get(String path, Class<T> responseType) {
        return request("GET", path, null, responseType);
    }
    
    /**
     * Make an HTTP request against the gateway base URL.
     *
     * <p>Retry policy (see {@link #execute}): never on any 4xx; only
     * {@link IOException}s and 5xx responses are retried, and only for idempotent
     * methods (GET/HEAD). Use {@link #request(String, String, Object, Class, String)}
     * to retry a write that carries an idempotency key.</p>
     */
    public <T> T request(String method, String path, Object body, Class<T> responseType) {
        return execute(client.getBaseUrl() + path, method, path, body, responseType, null);
    }

    /**
     * Like {@link #request(String, String, Object, Class)}, but sends the given
     * {@code Idempotency-Key} header, which makes the request safe to retry.
     */
    public <T> T request(String method, String path, Object body, Class<T> responseType, String idempotencyKey) {
        return execute(client.getBaseUrl() + path, method, path, body, responseType, idempotencyKey);
    }

    /** GET and HEAD are safe to repeat; anything else needs an idempotency key. */
    static boolean isRetriable(String method, String idempotencyKey) {
        if (idempotencyKey != null && !idempotencyKey.isEmpty()) {
            return true;
        }
        return "GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method);
    }

    /**
     * The one request loop behind {@link #request} and {@link #requestWithCustomBase}.
     *
     * <ul>
     *   <li>401/403 {@link AuthenticationException}, 429 {@link RateLimitException}
     *       (with {@code Retry-After}), other 4xx {@link ApiException}: thrown at once,
     *       never retried.</li>
     *   <li>5xx and {@link IOException}: retried up to {@code maxRetries} attempts
     *       (at least one), but only when {@link #isRetriable}; otherwise exactly one
     *       attempt. A 5xx that survives the attempts is rethrown as its
     *       {@link ApiException}.</li>
     *   <li>Bodies are sent in fixed-length streaming mode so {@link HttpURLConnection}
     *       cannot transparently re-send a POST on its own.</li>
     * </ul>
     */
    private <T> T execute(String url, String method, String path, Object body, Class<T> responseType,
                          String idempotencyKey) {
        int attempts = isRetriable(method, idempotencyKey) ? Math.max(1, client.getMaxRetries()) : 1;

        byte[] payload = null;
        if (body != null && !"GET".equals(method) && !"DELETE".equals(method)) {
            try {
                payload = objectMapper.writeValueAsBytes(body);
            } catch (JsonProcessingException e) {
                throw new SwfteException("Could not serialize request body: " + method + " " + path, e);
            }
        }

        for (int attempt = 0; ; attempt++) {
            boolean lastAttempt = attempt + 1 >= attempts;
            long retryAfterMs = -1;
            HttpURLConnection conn = null;
            try {
                conn = createConnection(url, method);
                if (idempotencyKey != null && !idempotencyKey.isEmpty()) {
                    conn.setRequestProperty("Idempotency-Key", idempotencyKey);
                }
                if (payload != null) {
                    conn.setDoOutput(true);
                    conn.setFixedLengthStreamingMode(payload.length);
                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(payload);
                    }
                } else if ("POST".equals(conn.getRequestMethod())) {
                    // Empty POSTs can still cause side effects; do not let the
                    // JDK buffer, repeat or redirect them after a lost response.
                    conn.setDoOutput(true);
                    conn.setFixedLengthStreamingMode(0);
                    conn.getOutputStream().close();
                } else {
                    conn.setDoOutput(false);
                }

                int code = conn.getResponseCode();
                if (code < 200 || code >= 300) {
                    Long retryAfter = parseRetryAfterSeconds(conn.getHeaderField("Retry-After"));
                    if (retryAfter != null) {
                        retryAfterMs = Math.min(retryAfter, 30L) * 1000L;
                    }
                    throw errorFor(conn, code, method, path);
                }

                if (responseType == Void.class || code == 204) {
                    return null;
                }
                String text = readResponseStream(conn);
                if (responseType == String.class) {
                    return responseType.cast(text);
                }
                if (text == null || text.isEmpty()) {
                    return null;
                }
                return objectMapper.readValue(text, responseType);
            } catch (ApiException e) {
                if (e.getStatusCode() < 500 || lastAttempt) {
                    throw e;
                }
            } catch (SwfteException e) {
                throw e;
            } catch (JsonProcessingException e) {
                throw new SwfteException("Invalid response body: " + method + " " + path, e);
            } catch (IOException e) {
                if (lastAttempt) {
                    String what = e instanceof java.net.SocketTimeoutException ? "Request timed out: " : "Request failed: ";
                    throw new SwfteException(what + method + " " + path
                        + (attempts > 1 ? " (after " + attempts + " attempts)" : ""), e);
                }
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

            try {
                Thread.sleep(retryAfterMs >= 0 ? retryAfterMs : (long) Math.pow(2, attempt) * 100);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new SwfteException("Request interrupted", ie);
            }
        }
    }

    /** Map a non-2xx response to its typed exception. Reads the error body. */
    private SwfteException errorFor(HttpURLConnection conn, int code, String method, String path) {
        String errorBody = readErrorStream(conn);
        String message = "API error: " + code + " " + method + " " + path
            + (errorBody.isEmpty() ? "" : " - " + errorBody);
        if (code == 401 || code == 403) {
            return new AuthenticationException(message);
        }
        if (code == 429) {
            return new RateLimitException(message, parseRetryAfterSeconds(conn.getHeaderField("Retry-After")));
        }
        return new ApiException(message, code, errorBody);
    }

    /** {@code Retry-After} as whole seconds; null when absent or an HTTP-date. */
    static Long parseRetryAfterSeconds(String header) {
        if (header == null) {
            return null;
        }
        try {
            long v = Long.parseLong(header.trim());
            return v < 0 ? null : v;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Make a streaming POST request.
     */
    public Stream<String> postStream(String path, Object body) {
        String url = client.getBaseUrl() + path;
        
        try {
            HttpURLConnection conn = createConnection(url, "POST");
            // Don't set Accept header - let the server determine response type

            String jsonBody = objectMapper.writeValueAsString(body);
            byte[] streamPayload = jsonBody.getBytes(StandardCharsets.UTF_8);
            // Streaming mode disables the JDK's hidden buffered-POST replay.
            conn.setFixedLengthStreamingMode(streamPayload.length);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(streamPayload);
            }
            
            int responseCode = conn.getResponseCode();
            
            if (responseCode >= 400) {
                throw errorFor(conn, responseCode, "POST", path);
            }
            
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
            );
            
            // Handle both "data:" and "data: " formats
            return reader.lines()
                .filter(line -> line.startsWith("data:"))
                .map(line -> line.substring(5).stripLeading())  // Remove "data:" prefix and leading whitespace
                .filter(data -> !data.equals("[DONE]"))
                .onClose(conn::disconnect);
                
        } catch (IOException e) {
            throw new SwfteException("Streaming request failed", e);
        }
    }
    
    /**
     * Make a POST request with multipart form data.
     */
    public <T> T postMultipart(String path, Map<String, Object> fields, Class<T> responseType) {
        return postMultipartUrl(client.getBaseUrl() + path, path, fields, responseType);
    }

    /** Multipart POST against the agents-service root, never the gateway. */
    public <T> T postMultipartWithCustomBase(String path, Map<String, Object> fields, Class<T> responseType) {
        return postMultipartUrl(client.getApiBaseUrl() + path, path, fields, responseType);
    }

    private <T> T postMultipartUrl(String url, String path, Map<String, Object> fields, Class<T> responseType) {
        String boundary = "----SwfteBoundary" + System.currentTimeMillis();
        
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(client.getTimeout());
            conn.setReadTimeout(client.getTimeout() * 2);
            conn.setRequestProperty("Authorization", "Bearer " + client.getApiKey());
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            conn.setRequestProperty("User-Agent", "swfte-java/" + SdkVersion.VERSION);
            
            if (client.getWorkspaceId() != null) {
                conn.setRequestProperty("X-Workspace-ID", client.getWorkspaceId());
            }

            // Write multipart incrementally without buffering a replayable POST.
            conn.setChunkedStreamingMode(8192);
            try (OutputStream os = conn.getOutputStream()) {
                for (Map.Entry<String, Object> entry : fields.entrySet()) {
                    os.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
                    
                    if (entry.getValue() instanceof byte[]) {
                        // Use audio.mp3 filename and audio/mpeg content-type for proper file format detection
                        os.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"; filename=\"audio.mp3\"\r\n").getBytes(StandardCharsets.UTF_8));
                        os.write("Content-Type: audio/mpeg\r\n\r\n".getBytes(StandardCharsets.UTF_8));
                        os.write((byte[]) entry.getValue());
                    } else {
                        os.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                        os.write(entry.getValue().toString().getBytes(StandardCharsets.UTF_8));
                    }
                    os.write("\r\n".getBytes(StandardCharsets.UTF_8));
                }
                os.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
            }
            
            int responseCode = conn.getResponseCode();
            
            if (responseCode >= 400) {
                throw errorFor(conn, responseCode, "POST", path);
            }
            
            String responseBody = readResponseStream(conn);
            return objectMapper.readValue(responseBody, responseType);
            
        } catch (IOException e) {
            throw new SwfteException("Multipart request failed", e);
        }
    }
    
    /**
     * Make a POST request and return raw bytes.
     */
    public byte[] postBytes(String path, Object body) {
        String url = client.getBaseUrl() + path;
        
        try {
            HttpURLConnection conn = createConnection(url, "POST");

            String jsonBody = objectMapper.writeValueAsString(body);
            byte[] bytesPayload = jsonBody.getBytes(StandardCharsets.UTF_8);
            // Keep byte-response calls subject to the same no-replay policy.
            conn.setFixedLengthStreamingMode(bytesPayload.length);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(bytesPayload);
            }
            
            int responseCode = conn.getResponseCode();
            
            if (responseCode >= 400) {
                throw errorFor(conn, responseCode, "POST", path);
            }
            
            return conn.getInputStream().readAllBytes();
            
        } catch (IOException e) {
            throw new SwfteException("Request failed", e);
        }
    }
    
    private HttpURLConnection createConnection(String url, String method) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        // HttpURLConnection doesn't natively support PATCH; use POST with X-HTTP-Method-Override
        if ("PATCH".equals(method)) {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("X-HTTP-Method-Override", "PATCH");
        } else {
            conn.setRequestMethod(method);
        }
        conn.setDoOutput(!"GET".equals(method));
        conn.setConnectTimeout(client.getTimeout());
        conn.setReadTimeout(client.getTimeout());
        conn.setRequestProperty("Authorization", "Bearer " + client.getApiKey());
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("User-Agent", "swfte-java/" + SdkVersion.VERSION);

        if (client.getWorkspaceId() != null) {
            conn.setRequestProperty("X-Workspace-ID", client.getWorkspaceId());
        }

        return conn;
    }
    
    private String readResponseStream(HttpURLConnection conn) throws IOException {
        try (InputStream in = conn.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String readErrorStream(HttpURLConnection conn) {
        try {
            if (conn.getErrorStream() != null) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    return response.toString();
                }
            }
        } catch (IOException e) {
            // Ignore
        }
        return "";
    }
    
    /**
     * One request against the agents-service API ({@link SwfteClient#getApiBaseUrl()}).
     *
     * <p>Never retried: it backs non-idempotent calls such as agent chat and
     * workflow invoke, where a silent retry could run (and bill) twice. Bodies
     * are sent in fixed-length streaming mode, which also stops
     * {@link HttpURLConnection} from transparently re-sending a POST.</p>
     *
     * <p>Errors: 401/403 {@link AuthenticationException}, 429 {@link RateLimitException},
     * any other non-2xx {@link ApiException} (status code and raw body).</p>
     *
     * @param path path (and query) relative to the API root, starting with {@code /}
     * @return the parsed body, or {@code null} for an empty body
     */
    public <T> T apiRequest(String method, String path, Object body, Class<T> responseType) {
        String url = client.getApiBaseUrl() + path;
        HttpURLConnection conn = null;
        try {
            conn = createConnection(url, method);
            if (body != null && !"GET".equals(method) && !"DELETE".equals(method)) {
                byte[] bytes = objectMapper.writeValueAsBytes(body);
                conn.setDoOutput(true);
                conn.setFixedLengthStreamingMode(bytes.length);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(bytes);
                }
            } else if ("POST".equals(conn.getRequestMethod())) {
                conn.setDoOutput(true);
                conn.setFixedLengthStreamingMode(0);
                conn.getOutputStream().close();
            } else {
                conn.setDoOutput(false);
            }

            int code = conn.getResponseCode();
            if (code < 200 || code >= 300) {
                throw errorFor(conn, code, method, path);
            }

            String text = code == 204 ? "" : readResponseStream(conn);
            if (text == null || text.isEmpty() || responseType == Void.class) {
                return null;
            }
            if (responseType == String.class) {
                return responseType.cast(text);
            }
            return objectMapper.readValue(text, responseType);
        } catch (SwfteException e) {
            throw e;
        } catch (java.net.SocketTimeoutException e) {
            throw new SwfteException("Request timed out: " + method + " " + path, e);
        } catch (IOException e) {
            throw new SwfteException("Request failed: " + method + " " + path, e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
    
    /**
     * Get the custom base URL (without the gateway path).
     */
    private String getCustomBaseUrl() {
        return client.getApiBaseUrl();
    }
    
    /**
     * Make a POST request with custom base URL.
     */
    public <T> T postWithCustomBase(String path, Object body, Class<T> responseType) {
        return requestWithCustomBase("POST", path, body, responseType);
    }
    
    /**
     * Make a GET request with custom base URL.
     */
    public <T> T getWithCustomBase(String path, Class<T> responseType) {
        return requestWithCustomBase("GET", path, null, responseType);
    }
    
    /**
     * Make a PUT request with custom base URL.
     */
    public <T> T putWithCustomBase(String path, Object body, Class<T> responseType) {
        return requestWithCustomBase("PUT", path, body, responseType);
    }

    /**
     * Make a PATCH request with custom base URL.
     */
    public <T> T patchWithCustomBase(String path, Object body, Class<T> responseType) {
        return requestWithCustomBase("PATCH", path, body, responseType);
    }
    
    /**
     * Make a DELETE request with custom base URL.
     */
    public void deleteWithCustomBase(String path) {
        requestWithCustomBase("DELETE", path, null, Void.class);
    }
    
    /**
     * Make an HTTP request against the agents-service root. Same retry policy as
     * {@link #request(String, String, Object, Class)}.
     */
    public <T> T requestWithCustomBase(String method, String path, Object body, Class<T> responseType) {
        return execute(getCustomBaseUrl() + path, method, path, body, responseType, null);
    }

    /** Like {@link #requestWithCustomBase(String, String, Object, Class)} with an idempotency key. */
    public <T> T requestWithCustomBase(String method, String path, Object body, Class<T> responseType,
                                       String idempotencyKey) {
        return execute(getCustomBaseUrl() + path, method, path, body, responseType, idempotencyKey);
    }
}
