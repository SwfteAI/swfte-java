package com.swfte.sdk.resources;

import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.HttpClient;
import com.swfte.sdk.models.Dataset;

import java.util.HashMap;
import java.util.Map;

/**
 * Datasets API resource — manage RAG knowledge bases.
 *
 * <p>See <a href="https://www.swfte.com/products/rag">swfte.com/products/rag</a>.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * Dataset dataset = client.datasets().create(
 *     Dataset.builder()
 *         .name("Product Docs")
 *         .description("Public product documentation")
 *         .embeddingModel("openai:text-embedding-3-small")
 *         .build()
 * );
 *
 * client.datasets().toggleApiAccess(dataset.getId(), "ENABLED");
 * }</pre>
 */
public class Datasets {

    private final HttpClient httpClient;
    private final SwfteClient client;

    public Datasets(SwfteClient client) {
        this.client = client;
        this.httpClient = new HttpClient(client);
    }

    private String base() { return "/api/v2/datasets"; }

    /** List datasets. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> list() { return list(0, 20); }

    @SuppressWarnings("unchecked")
    public Map<String, Object> list(int page, int size) {
        return httpClient.getWithCustomBase(base() + "?page=" + page + "&size=" + size, Map.class);
    }

    /** Create a dataset. */
    public Dataset create(Dataset dataset) {
        return httpClient.postWithCustomBase(base(), dataset, Dataset.class);
    }

    /** Get a dataset by ID. */
    public Dataset get(String datasetId) {
        return httpClient.getWithCustomBase(base() + "/" + datasetId, Dataset.class);
    }

    /** Update a dataset. */
    public Dataset update(String datasetId, Dataset updates) {
        return httpClient.putWithCustomBase(base() + "/" + datasetId, updates, Dataset.class);
    }

    /** Delete a dataset. */
    public void delete(String datasetId) {
        httpClient.deleteWithCustomBase(base() + "/" + datasetId);
    }

    /** Check whether a dataset is in use. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> useCheck(String datasetId) {
        return httpClient.getWithCustomBase(base() + "/" + datasetId + "/use-check", Map.class);
    }

    /**
     * Toggle external API access for a dataset.
     *
     * @param datasetId the dataset ID
     * @param status the new status (e.g. {@code "ENABLED"} or {@code "DISABLED"})
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> toggleApiAccess(String datasetId, String status) {
        return httpClient.postWithCustomBase(
            base() + "/" + datasetId + "/api-access/" + status,
            new HashMap<>(),
            Map.class
        );
    }
}
