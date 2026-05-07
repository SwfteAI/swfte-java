package com.swfte.sdk.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Represents a connected Model Context Protocol (MCP) server.
 */
public class MCPServer {

    private String providerId;
    private String name;
    private String url;
    private String transport;
    private String status;
    private List<String> toolIds;
    private Map<String, Object> config;
    private Map<String, Object> auth;
    private LocalDateTime connectedAt;

    public MCPServer() {}

    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getTransport() { return transport; }
    public void setTransport(String transport) { this.transport = transport; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<String> getToolIds() { return toolIds; }
    public void setToolIds(List<String> toolIds) { this.toolIds = toolIds; }
    public Map<String, Object> getConfig() { return config; }
    public void setConfig(Map<String, Object> config) { this.config = config; }
    public Map<String, Object> getAuth() { return auth; }
    public void setAuth(Map<String, Object> auth) { this.auth = auth; }
    public LocalDateTime getConnectedAt() { return connectedAt; }
    public void setConnectedAt(LocalDateTime connectedAt) { this.connectedAt = connectedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final MCPServer s = new MCPServer();
        public Builder providerId(String v) { s.providerId = v; return this; }
        public Builder name(String v) { s.name = v; return this; }
        public Builder url(String v) { s.url = v; return this; }
        public Builder transport(String v) { s.transport = v; return this; }
        public Builder config(Map<String, Object> v) { s.config = v; return this; }
        public Builder auth(Map<String, Object> v) { s.auth = v; return this; }
        public MCPServer build() { return s; }
    }
}
