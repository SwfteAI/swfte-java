package com.swfte.sdk.resources;

import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.HttpClient;
import com.swfte.sdk.models.Model;
import com.swfte.sdk.models.ModelsResponse;

import java.util.List;

/**
 * Models API resource.
 */
public class Models {
    
    private final HttpClient httpClient;
    
    public Models(SwfteClient client) {
        this.httpClient = new HttpClient(client);
    }
    
    /**
     * List available models.
     *
     * @return list of models
     */
    public List<Model> list() {
        ModelsResponse response = httpClient.get("/models", ModelsResponse.class);
        return response.getModelList();
    }

    /**
     * List available models and return full response.
     *
     * @return models response with metadata
     */
    public ModelsResponse listResponse() {
        return httpClient.get("/models", ModelsResponse.class);
    }
    
    /**
     * Retrieve a specific model.
     *
     * @param modelId the model ID
     * @return the model
     */
    public Model retrieve(String modelId) {
        return httpClient.get("/models/" + modelId, Model.class);
    }
}

