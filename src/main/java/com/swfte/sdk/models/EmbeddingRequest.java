package com.swfte.sdk.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Request for embeddings.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmbeddingRequest {
    
    private String model;
    private Object input;
    
    @JsonProperty("encoding_format")
    private String encodingFormat;
    
    private EmbeddingRequest(Builder builder) {
        this.model = builder.model;
        this.input = builder.input;
        this.encodingFormat = builder.encodingFormat;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Getters
    public String getModel() {
        return model;
    }
    
    public Object getInput() {
        return input;
    }
    
    public String getEncodingFormat() {
        return encodingFormat;
    }
    
    /**
     * Builder for EmbeddingRequest.
     */
    public static class Builder {
        private String model;
        private Object input;
        private String encodingFormat = "float";
        
        public Builder model(String model) {
            this.model = model;
            return this;
        }
        
        public Builder input(String input) {
            this.input = input;
            return this;
        }
        
        public Builder input(List<String> input) {
            this.input = input;
            return this;
        }
        
        public Builder encodingFormat(String encodingFormat) {
            this.encodingFormat = encodingFormat;
            return this;
        }
        
        public EmbeddingRequest build() {
            return new EmbeddingRequest(this);
        }
    }
}

