package com.swfte.sdk.resources;

import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.HttpClient;
import com.swfte.sdk.models.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Documents API resource — manage documents inside a Swfte dataset.
 *
 * <p>See <a href="https://www.swfte.com/products/rag">swfte.com/products/rag</a>.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * Document doc = client.documents().create("dataset-12345", List.of(
 *     Document.builder()
 *         .name("Pricing FAQ")
 *         .dataSourceType("TEXT")
 *         .content("Our pricing is simple...")
 *         .build()
 * ));
 * }</pre>
 */
public class Documents {

    private final HttpClient httpClient;
    private final SwfteClient client;

    public Documents(SwfteClient client) {
        this.client = client;
        this.httpClient = new HttpClient(client);
    }

    private String base(String datasetId) {
        return "/api/v2/datasets/" + datasetId + "/documents";
    }

    /** Create one or more documents in a dataset. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> create(String datasetId, List<Document> documents) {
        Map<String, Object> body = new HashMap<>();
        body.put("documents", documents);
        return httpClient.postWithCustomBase(base(datasetId), body, Map.class);
    }

    /** List documents in a dataset. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> list(String datasetId) { return list(datasetId, 0, 20); }

    @SuppressWarnings("unchecked")
    public Map<String, Object> list(String datasetId, int page, int size) {
        return httpClient.getWithCustomBase(
            base(datasetId) + "?page=" + page + "&size=" + size,
            Map.class
        );
    }

    /** Get a single document. */
    public Document get(String datasetId, String documentId) {
        return httpClient.getWithCustomBase(base(datasetId) + "/" + documentId, Document.class);
    }

    /** Update a document. */
    public Document update(String datasetId, String documentId, Document updates) {
        return httpClient.putWithCustomBase(
            base(datasetId) + "/" + documentId,
            updates,
            Document.class
        );
    }

    /** Delete a document. */
    public void delete(String datasetId, String documentId) {
        httpClient.deleteWithCustomBase(base(datasetId) + "/" + documentId);
    }

    /** Get parsed segments for a document. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> segments(String datasetId, String documentId) {
        return httpClient.getWithCustomBase(base(datasetId) + "/" + documentId + "/segments", Map.class);
    }

    /** Retry processing for a document. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> retry(String datasetId, String documentId) {
        return httpClient.postWithCustomBase(
            base(datasetId) + "/" + documentId + "/retry",
            new HashMap<>(),
            Map.class
        );
    }

    /** Get the processing status for the dataset. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> processingStatus(String datasetId) {
        return httpClient.getWithCustomBase(base(datasetId) + "/processing-status", Map.class);
    }

    /** Pause document processing. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> pause(String datasetId, String documentId) {
        return httpClient.postWithCustomBase(
            base(datasetId) + "/" + documentId + "/pause",
            new HashMap<>(),
            Map.class
        );
    }

    /** Resume document processing. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> resume(String datasetId, String documentId) {
        return httpClient.postWithCustomBase(
            base(datasetId) + "/" + documentId + "/resume",
            new HashMap<>(),
            Map.class
        );
    }

    /** Batch update documents. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> batchUpdate(String datasetId, Map<String, Object> body) {
        return httpClient.patchWithCustomBase(base(datasetId) + "/batch", body, Map.class);
    }

    /** Get batch status. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> batchStatus(String datasetId, String batch) {
        return httpClient.getWithCustomBase(
            base(datasetId) + "/batch/" + batch + "/status",
            Map.class
        );
    }
}
