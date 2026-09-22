package com.swfte.sdk.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.Map;

/**
 * Response of {@code GET /v2/catalog/{kind}/{id}/contract}: how to call an artifact.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class CatalogContract {

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static class Invoke {
        private String method;
        private String path;
        private String auth;
        @com.fasterxml.jackson.annotation.JsonProperty("async")
        private boolean async;
        private String statusPath;

        public String getMethod() { return method; }
        public String getPath() { return path; }
        /** pat, api_key or public. */
        public String getAuth() { return auth; }
        public boolean isAsync() { return async; }
        /** Where to poll when {@link #isAsync()}; {@code null} otherwise. */
        public String getStatusPath() { return statusPath; }
    }

    private String catalogRef;
    private Invoke invoke;
    private Map<String, Object> inputSchema;
    private Map<String, Object> outputSchema;
    private Map<String, String> snippets;
    private Map<String, String> embed;

    public String getCatalogRef() { return catalogRef; }
    public Invoke getInvoke() { return invoke; }
    /** JSON Schema of the inputs. */
    public Map<String, Object> getInputSchema() { return inputSchema; }
    /** JSON Schema of the outputs. */
    public Map<String, Object> getOutputSchema() { return outputSchema; }
    /** {@code curl}, {@code typescript}, {@code python}, {@code mcp}. */
    public Map<String, String> getSnippets() { return snippets == null ? Collections.emptyMap() : snippets; }
    /** {@code {html}} for embeddable artifacts, else {@code null}. */
    public Map<String, String> getEmbed() { return embed; }
}
