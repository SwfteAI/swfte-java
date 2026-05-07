package com.swfte.sdk.resources;

import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.HttpClient;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Audit API resource — query the platform-wide audit log.
 *
 * <p>See <a href="https://www.swfte.com/products/security">swfte.com/products/security</a>.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * Map<String, Object> events = client.audit().listEvents(Map.of(
 *     "actorId", "user-123",
 *     "from", "2026-05-01T00:00:00Z"
 * ));
 * }</pre>
 */
public class Audit {

    private final HttpClient httpClient;
    private final SwfteClient client;

    public Audit(SwfteClient client) {
        this.client = client;
        this.httpClient = new HttpClient(client);
    }

    private String base() { return "/v2/audit"; }

    /** List events with optional filters (actorId, action, from, to, etc.). */
    @SuppressWarnings("unchecked")
    public Map<String, Object> listEvents(Map<String, Object> filters) {
        return httpClient.getWithCustomBase(base() + "/events" + qs(filters), Map.class);
    }

    /** List events for a specific resource. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> resourceEvents(String resourceType, String resourceId) {
        return httpClient.getWithCustomBase(
            base() + "/events/" + resourceType + "/" + resourceId,
            Map.class
        );
    }

    /** List events attributed to the current authenticated principal. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> myEvents() {
        return httpClient.getWithCustomBase(base() + "/events/me", Map.class);
    }

    /** Export events. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> export(Map<String, Object> filters) {
        return httpClient.getWithCustomBase(base() + "/export" + qs(filters), Map.class);
    }

    private String qs(Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) return "";
        StringBuilder sb = new StringBuilder("?");
        Map<String, Object> ordered = new LinkedHashMap<>(filters);
        boolean first = true;
        for (Map.Entry<String, Object> e : ordered.entrySet()) {
            if (!first) sb.append("&");
            first = false;
            sb.append(encode(e.getKey()));
            sb.append("=");
            sb.append(encode(String.valueOf(e.getValue())));
        }
        return sb.toString();
    }

    private String encode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            return s;
        }
    }
}
