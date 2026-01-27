package com.swfte.sdk.resources;

import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.HttpClient;
import com.swfte.sdk.models.EmbeddingRequest;
import com.swfte.sdk.models.EmbeddingResponse;

/**
 * Embeddings API resource.
 */
public class Embeddings {
    
    private final HttpClient httpClient;
    
    public Embeddings(SwfteClient client) {
        this.httpClient = new HttpClient(client);
    }
    
    /**
     * Create embeddings for the input text(s).
     *
     * @param request the embedding request
     * @return embedding response with vectors
     */
    public EmbeddingResponse create(EmbeddingRequest request) {
        return httpClient.post("/embeddings", request, EmbeddingResponse.class);
    }
}

