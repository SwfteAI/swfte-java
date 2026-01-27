package com.swfte.sdk.models;

import java.util.List;

/**
 * Response from models list.
 */
public class ModelsResponse {
    
    private String object;
    private List<Model> data;
    private List<Model> models;
    
    public String getObject() {
        return object;
    }
    
    public void setObject(String object) {
        this.object = object;
    }
    
    public List<Model> getData() {
        return data;
    }
    
    public void setData(List<Model> data) {
        this.data = data;
    }
    
    public List<Model> getModels() {
        return models;
    }
    
    public void setModels(List<Model> models) {
        this.models = models;
    }
    
    /**
     * Get the list of models (handles both formats).
     */
    public List<Model> getModelList() {
        return data != null ? data : models;
    }
}

