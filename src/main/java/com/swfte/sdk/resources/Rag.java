package com.swfte.sdk.resources;

import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.HttpClient;
import com.swfte.sdk.models.RagSearchRequest;
import com.swfte.sdk.models.RagSearchResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RAG (Retrieval-Augmented Generation) advanced API — hybrid search,
 * rerankers, embedding model catalogue, BM25 vocabulary build.
 *
 * <p>See <a href="https://www.swfte.com/products/rag">swfte.com/products/rag</a>.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * RagSearchResponse results = client.rag().search(
 *     RagSearchRequest.builder()
 *         .query("How do refunds work?")
 *         .datasetIds(List.of("dataset-12345"))
 *         .topK(8)
 *         .rerank(true)
 *         .build()
 * );
 * }</pre>
 */
public class Rag {

    private final HttpClient httpClient;
    private final SwfteClient client;

    public Rag(SwfteClient client) {
        this.client = client;
        this.httpClient = new HttpClient(client);
    }

    private String base() { return "/v2/rag"; }

    /** Hybrid (vector + lexical) RAG search across one or more datasets. */
    public RagSearchResponse search(RagSearchRequest request) {
        return httpClient.postWithCustomBase(base() + "/search", request, RagSearchResponse.class);
    }

    /** Rerank a list of candidates with a reranker model. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> rerank(String query, List<Map<String, Object>> documents, String rerankerModel) {
        Map<String, Object> body = new HashMap<>();
        body.put("query", query);
        body.put("documents", documents);
        if (rerankerModel != null) body.put("rerankerModel", rerankerModel);
        return httpClient.postWithCustomBase(base() + "/rerank", body, Map.class);
    }

    /** List embedding models available in this workspace. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> embeddingModels() {
        return httpClient.getWithCustomBase(base() + "/models/embeddings", Map.class);
    }

    /** List reranker models available in this workspace. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> rerankerModels() {
        return httpClient.getWithCustomBase(base() + "/models/rerankers", Map.class);
    }

    /** List supported retrieval strategies. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> strategies() {
        return httpClient.getWithCustomBase(base() + "/strategies", Map.class);
    }

    /** Get the workspace's RAG config. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> config() {
        return httpClient.getWithCustomBase(base() + "/config", Map.class);
    }

    /** Trigger a build of the BM25 vocabulary. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> buildVocabulary(Map<String, Object> body) {
        return httpClient.postWithCustomBase(
            base() + "/vocabulary/build",
            body != null ? body : new HashMap<>(),
            Map.class
        );
    }

    /** Get vocabulary statistics. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> vocabularyStats() {
        return httpClient.getWithCustomBase(base() + "/vocabulary/stats", Map.class);
    }
}
