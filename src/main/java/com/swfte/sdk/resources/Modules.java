package com.swfte.sdk.resources;

import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.HttpClient;
import com.swfte.sdk.models.Module;
import com.swfte.sdk.models.ModuleVersion;

import java.util.HashMap;
import java.util.Map;

/**
 * Modules API resource — bundle agents, workflows, tools, datasets, and chatflows
 * as reusable, versionable units.
 *
 * <p>See <a href="https://www.swfte.com/marketplace">swfte.com/marketplace</a>.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * Module module = client.modules().create(
 *     Module.builder().name("Sales Suite").description("Lead capture + qualification").build()
 * );
 *
 * client.modules().addResource(module.getId(), Map.of("type", "AGENT", "id", "agent-123"));
 * client.modules().build(module.getId());
 * }</pre>
 */
public class Modules {

    private final HttpClient httpClient;
    private final SwfteClient client;

    public Modules(SwfteClient client) {
        this.client = client;
        this.httpClient = new HttpClient(client);
    }

    private String base() { return "/v2/modules"; }

    /** List modules. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> list() {
        return httpClient.getWithCustomBase(base(), Map.class);
    }

    /** Create a module. */
    public Module create(Module module) {
        return httpClient.postWithCustomBase(base(), module, Module.class);
    }

    /** Get a module. */
    public Module get(String moduleId) {
        return httpClient.getWithCustomBase(base() + "/" + moduleId, Module.class);
    }

    /** Update a module. */
    public Module update(String moduleId, Module updates) {
        return httpClient.putWithCustomBase(base() + "/" + moduleId, updates, Module.class);
    }

    /** Delete a module. */
    public void delete(String moduleId) {
        httpClient.deleteWithCustomBase(base() + "/" + moduleId);
    }

    /** Add a resource to a module. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> addResource(String moduleId, Map<String, Object> resource) {
        return httpClient.postWithCustomBase(
            base() + "/" + moduleId + "/resources",
            resource,
            Map.class
        );
    }

    /** Remove a resource from a module. */
    public void removeResource(String moduleId, String resourceId) {
        httpClient.deleteWithCustomBase(base() + "/" + moduleId + "/resources/" + resourceId);
    }

    /** Build (compile) a module into a deployable bundle. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> build(String moduleId) {
        return httpClient.postWithCustomBase(
            base() + "/" + moduleId + "/build",
            new HashMap<>(),
            Map.class
        );
    }

    /**
     * Build progress endpoint URL for SSE streaming. Use this if your client
     * wants to subscribe to a Server-Sent-Events stream of build progress.
     */
    public String buildProgressUrl(String moduleId) {
        return client.getBaseUrl().replaceAll("/v[12]/gateway$", "")
            + base() + "/" + moduleId + "/build/progress";
    }

    /** List versions of a module. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> listVersions(String moduleId) {
        return httpClient.getWithCustomBase(base() + "/" + moduleId + "/versions", Map.class);
    }

    /** Get a single module version. */
    public ModuleVersion getVersion(String moduleId, String versionNumber) {
        return httpClient.getWithCustomBase(
            base() + "/" + moduleId + "/versions/" + versionNumber,
            ModuleVersion.class
        );
    }

    /** Get the QA bank for a version. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> versionQa(String moduleId, String versionNumber) {
        return httpClient.getWithCustomBase(
            base() + "/" + moduleId + "/versions/" + versionNumber + "/qa",
            Map.class
        );
    }

    /** Get the impact report for a module. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> impact(String moduleId) {
        return httpClient.getWithCustomBase(base() + "/" + moduleId + "/impact", Map.class);
    }
}
