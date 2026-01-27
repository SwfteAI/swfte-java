package com.swfte.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.swfte.sdk.exceptions.AuthenticationException;
import com.swfte.sdk.exceptions.RateLimitException;
import com.swfte.sdk.exceptions.ApiException;
import com.swfte.sdk.exceptions.SwfteException;

import java.io.BufferedReader;
import java.io.IOException;
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
     * Make an HTTP request with retry logic.
     */
    public <T> T request(String method, String path, Object body, Class<T> responseType) {
        String url = client.getBaseUrl() + path;
        Exception lastException = null;
        
        for (int attempt = 0; attempt < client.getMaxRetries(); attempt++) {
            try {
                HttpURLConnection conn = createConnection(url, method);
                
                if (body != null) {
                    String jsonBody = objectMapper.writeValueAsString(body);
                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                    }
                }
                
                int responseCode = conn.getResponseCode();
                
                if (responseCode == 401) {
                    throw new AuthenticationException("Invalid API key");
                } else if (responseCode == 429) {
                    throw new RateLimitException("Rate limit exceeded");
                } else if (responseCode >= 400) {
                    String errorBody = readErrorStream(conn);
                    throw new ApiException("API error: " + responseCode + " - " + errorBody, responseCode);
                }
                
                if (responseType == String.class) {
                    return responseType.cast(readResponseStream(conn));
                }
                
                String responseBody = readResponseStream(conn);
                return objectMapper.readValue(responseBody, responseType);
                
            } catch (AuthenticationException | RateLimitException e) {
                throw e;
            } catch (Exception e) {
                lastException = e;
                if (attempt < client.getMaxRetries() - 1) {
                    try {
                        Thread.sleep((long) Math.pow(2, attempt) * 100);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new SwfteException("Request interrupted", ie);
                    }
                }
            }
        }
        
        throw new SwfteException("Request failed after " + client.getMaxRetries() + " attempts", lastException);
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
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }
            
            int responseCode = conn.getResponseCode();
            
            if (responseCode == 401) {
                throw new AuthenticationException("Invalid API key");
            } else if (responseCode == 429) {
                throw new RateLimitException("Rate limit exceeded");
            } else if (responseCode >= 400) {
                String errorBody = readErrorStream(conn);
                throw new ApiException("API error: " + responseCode + " - " + errorBody, responseCode);
            }
            
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
            );
            
            // Handle both "data:" and "data: " formats
            return reader.lines()
                .filter(line -> line.startsWith("data:"))
                .map(line -> line.substring(5).stripLeading())  // Remove "data:" prefix and leading whitespace
                .filter(data -> !data.equals("[DONE]"));
                
        } catch (IOException e) {
            throw new SwfteException("Streaming request failed", e);
        }
    }
    
    /**
     * Make a POST request with multipart form data.
     */
    public <T> T postMultipart(String path, Map<String, Object> fields, Class<T> responseType) {
        String url = client.getBaseUrl() + path;
        String boundary = "----SwfteBoundary" + System.currentTimeMillis();
        
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(client.getTimeout());
            conn.setReadTimeout(client.getTimeout() * 2);
            conn.setRequestProperty("Authorization", "Bearer " + client.getApiKey());
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            conn.setRequestProperty("User-Agent", "swfte-java/1.0.0");
            
            if (client.getWorkspaceId() != null) {
                conn.setRequestProperty("X-Workspace-ID", client.getWorkspaceId());
            }
            
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
                String errorBody = readErrorStream(conn);
                throw new ApiException("API error: " + responseCode + " - " + errorBody, responseCode);
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
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }
            
            int responseCode = conn.getResponseCode();
            
            if (responseCode >= 400) {
                String errorBody = readErrorStream(conn);
                throw new ApiException("API error: " + responseCode + " - " + errorBody, responseCode);
            }
            
            return conn.getInputStream().readAllBytes();
            
        } catch (IOException e) {
            throw new SwfteException("Request failed", e);
        }
    }
    
    private HttpURLConnection createConnection(String url, String method) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod(method);
        conn.setDoOutput(!"GET".equals(method));
        conn.setConnectTimeout(client.getTimeout());
        conn.setReadTimeout(client.getTimeout());
        conn.setRequestProperty("Authorization", "Bearer " + client.getApiKey());
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("User-Agent", "swfte-java/1.0.0");
        
        if (client.getWorkspaceId() != null) {
            conn.setRequestProperty("X-Workspace-ID", client.getWorkspaceId());
        }
        
        return conn;
    }
    
    private String readResponseStream(HttpURLConnection conn) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
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
    
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
    
    /**
     * Get the custom base URL (without the gateway path).
     */
    private String getCustomBaseUrl() {
        String base = client.getBaseUrl();
        if (base.contains("/gateway")) {
            base = base.replace("/v1/gateway", "");
        }
        return base;
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
     * Make a DELETE request with custom base URL.
     */
    public void deleteWithCustomBase(String path) {
        requestWithCustomBase("DELETE", path, null, Void.class);
    }
    
    /**
     * Make an HTTP request with custom base URL and retry logic.
     */
    public <T> T requestWithCustomBase(String method, String path, Object body, Class<T> responseType) {
        String url = getCustomBaseUrl() + path;
        Exception lastException = null;
        
        for (int attempt = 0; attempt < client.getMaxRetries(); attempt++) {
            try {
                HttpURLConnection conn = createConnection(url, method);
                
                if (body != null && !"GET".equals(method) && !"DELETE".equals(method)) {
                    String jsonBody = objectMapper.writeValueAsString(body);
                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                    }
                }
                
                int responseCode = conn.getResponseCode();
                
                if (responseCode == 401) {
                    throw new AuthenticationException("Invalid API key");
                } else if (responseCode == 429) {
                    throw new RateLimitException("Rate limit exceeded");
                } else if (responseCode >= 400) {
                    String errorBody = readErrorStream(conn);
                    throw new ApiException("API error: " + responseCode + " - " + errorBody, responseCode);
                }
                
                if (responseType == Void.class || responseCode == 204) {
                    return null;
                }
                
                if (responseType == String.class) {
                    return responseType.cast(readResponseStream(conn));
                }
                
                String responseBody = readResponseStream(conn);
                if (responseBody == null || responseBody.isEmpty()) {
                    return null;
                }
                return objectMapper.readValue(responseBody, responseType);
                
            } catch (AuthenticationException | RateLimitException e) {
                throw e;
            } catch (Exception e) {
                lastException = e;
                if (attempt < client.getMaxRetries() - 1) {
                    try {
                        Thread.sleep((long) Math.pow(2, attempt) * 100);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new SwfteException("Request interrupted", ie);
                    }
                }
            }
        }
        
        throw new SwfteException("Request failed after " + client.getMaxRetries() + " attempts", lastException);
    }
}

