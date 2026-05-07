package com.swfte.sdk.models;

import java.util.Map;

/**
 * Represents an MCP tool exposed by a connected server.
 */
public class MCPTool {

    private String id;
    private String name;
    private String description;
    private String providerId;
    private Map<String, Object> inputSchema;
    private Map<String, Object> outputSchema;
    private String status;

    public MCPTool() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }
    public Map<String, Object> getInputSchema() { return inputSchema; }
    public void setInputSchema(Map<String, Object> inputSchema) { this.inputSchema = inputSchema; }
    public Map<String, Object> getOutputSchema() { return outputSchema; }
    public void setOutputSchema(Map<String, Object> outputSchema) { this.outputSchema = outputSchema; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final MCPTool t = new MCPTool();
        public Builder id(String v) { t.id = v; return this; }
        public Builder name(String v) { t.name = v; return this; }
        public Builder description(String v) { t.description = v; return this; }
        public Builder providerId(String v) { t.providerId = v; return this; }
        public MCPTool build() { return t; }
    }
}
