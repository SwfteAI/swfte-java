package com.swfte.sdk.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response from embeddings.
 */
public class EmbeddingResponse {
    
    private String object;
    private String model;
    private List<Embedding> data;
    private Usage usage;
    
    public String getObject() {
        return object;
    }
    
    public void setObject(String object) {
        this.object = object;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public List<Embedding> getData() {
        return data;
    }
    
    public void setData(List<Embedding> data) {
        this.data = data;
    }
    
    public Usage getUsage() {
        return usage;
    }
    
    public void setUsage(Usage usage) {
        this.usage = usage;
    }
    
    /**
     * An embedding.
     */
    public static class Embedding {
        private String object;
        private int index;
        private List<Double> embedding;
        
        public String getObject() {
            return object;
        }
        
        public void setObject(String object) {
            this.object = object;
        }
        
        public int getIndex() {
            return index;
        }
        
        public void setIndex(int index) {
            this.index = index;
        }
        
        public List<Double> getEmbedding() {
            return embedding;
        }
        
        public void setEmbedding(List<Double> embedding) {
            this.embedding = embedding;
        }
    }
    
    /**
     * Embedding usage statistics.
     */
    public static class Usage {
        @JsonProperty("prompt_tokens")
        private int promptTokens;
        
        @JsonProperty("total_tokens")
        private int totalTokens;
        
        public int getPromptTokens() {
            return promptTokens;
        }
        
        public void setPromptTokens(int promptTokens) {
            this.promptTokens = promptTokens;
        }
        
        public int getTotalTokens() {
            return totalTokens;
        }
        
        public void setTotalTokens(int totalTokens) {
            this.totalTokens = totalTokens;
        }
    }
}

