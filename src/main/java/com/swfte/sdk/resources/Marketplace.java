package com.swfte.sdk.resources;

import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.HttpClient;
import com.swfte.sdk.models.Installation;
import com.swfte.sdk.models.Publication;

import java.util.HashMap;
import java.util.Map;

/**
 * Marketplace API resource — browse, install, and uninstall published modules.
 *
 * <p>See <a href="https://www.swfte.com/marketplace">swfte.com/marketplace</a>.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * Map<String, Object> publications = client.marketplace().browse();
 * Installation installation = client.marketplace().install("pub-12345", Map.of("variables", Map.of("region", "EU")));
 * }</pre>
 */
public class Marketplace {

    private final HttpClient httpClient;
    private final SwfteClient client;

    public Marketplace(SwfteClient client) {
        this.client = client;
        this.httpClient = new HttpClient(client);
    }

    private String base() { return "/v2/marketplace"; }

    /** Browse marketplace publications. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> browse() {
        return httpClient.getWithCustomBase(base(), Map.class);
    }

    /** Get publication detail. */
    public Publication get(String publicationId) {
        return httpClient.getWithCustomBase(base() + "/" + publicationId, Publication.class);
    }

    /** Install a publication into the current workspace. */
    public Installation install(String publicationId, Map<String, Object> body) {
        return httpClient.postWithCustomBase(
            base() + "/" + publicationId + "/install",
            body != null ? body : new HashMap<>(),
            Installation.class
        );
    }

    /** List installations in the current workspace. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> installations() {
        return httpClient.getWithCustomBase(base() + "/installations", Map.class);
    }

    /** Uninstall an installation. */
    public void uninstall(String installationId) {
        httpClient.deleteWithCustomBase(base() + "/installations/" + installationId);
    }
}
