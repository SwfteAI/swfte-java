package com.swfte.sdk.resources;

import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.HttpClient;
import com.swfte.sdk.models.Agent;

import java.util.HashMap;
import java.util.Map;

/**
 * Agent Wizard API resource — generate, review, refine, and persist agents from
 * a natural-language prompt or template.
 *
 * <p>See <a href="https://www.swfte.com/products/agents">swfte.com/products/agents</a>.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * Map<String, Object> generated = client.agentWizard().generate(Map.of(
 *     "prompt", "A research assistant that summarises arxiv papers"
 * ));
 *
 * Agent agent = client.agentWizard().create(generated);
 * }</pre>
 */
public class AgentWizard {

    private final HttpClient httpClient;
    private final SwfteClient client;

    public AgentWizard(SwfteClient client) {
        this.client = client;
        this.httpClient = new HttpClient(client);
    }

    private String base() { return "/v2/agents/wizard"; }

    /** Generate an agent draft from a prompt. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> generate(Map<String, Object> body) {
        return httpClient.postWithCustomBase(base() + "/generate", body, Map.class);
    }

    /** Quick (no review) generate. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> quick(Map<String, Object> body) {
        return httpClient.postWithCustomBase(base() + "/quick", body, Map.class);
    }

    /** Review a generated agent. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> review(Map<String, Object> body) {
        return httpClient.postWithCustomBase(base() + "/review", body, Map.class);
    }

    /** Refine via feedback. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> refine(Map<String, Object> body) {
        return httpClient.postWithCustomBase(base() + "/refine", body, Map.class);
    }

    /** Persist a generated agent draft. */
    public Agent create(Map<String, Object> body) {
        return httpClient.postWithCustomBase(base() + "/create", body, Agent.class);
    }

    /** Link MCP tools to an agent. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> linkTools(Map<String, Object> body) {
        return httpClient.postWithCustomBase(base() + "/link-tools", body, Map.class);
    }

    /** Link knowledge bases to an agent. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> linkKnowledge(Map<String, Object> body) {
        return httpClient.postWithCustomBase(base() + "/link-knowledge", body, Map.class);
    }

    /** List available templates. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> templates() {
        return httpClient.getWithCustomBase(base() + "/templates", Map.class);
    }

    /** Get a single template by name. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> template(String name) {
        return httpClient.getWithCustomBase(base() + "/templates/" + name, Map.class);
    }

    /** List supported agent types. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> agentTypes() {
        return httpClient.getWithCustomBase(base() + "/agent-types", Map.class);
    }

    /** List available providers. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> providers() {
        return httpClient.getWithCustomBase(base() + "/providers", Map.class);
    }

    /** Create an agent from a named template. */
    public Agent fromTemplate(String templateName, Map<String, Object> overrides) {
        return httpClient.postWithCustomBase(
            base() + "/from-template/" + templateName,
            overrides != null ? overrides : new HashMap<>(),
            Agent.class
        );
    }
}
