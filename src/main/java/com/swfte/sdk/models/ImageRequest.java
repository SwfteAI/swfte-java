package com.swfte.sdk.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request for image generation.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImageRequest {
    
    private String model;
    private String prompt;
    private Integer n;
    private String size;
    private String quality;
    private String style;
    
    @JsonProperty("response_format")
    private String responseFormat;
    
    @JsonProperty("negative_prompt")
    private String negativePrompt;
    
    private ImageRequest(Builder builder) {
        this.model = builder.model;
        this.prompt = builder.prompt;
        this.n = builder.n;
        this.size = builder.size;
        this.quality = builder.quality;
        this.style = builder.style;
        this.responseFormat = builder.responseFormat;
        this.negativePrompt = builder.negativePrompt;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Getters
    public String getModel() {
        return model;
    }
    
    public String getPrompt() {
        return prompt;
    }
    
    public Integer getN() {
        return n;
    }
    
    public String getSize() {
        return size;
    }
    
    public String getQuality() {
        return quality;
    }
    
    public String getStyle() {
        return style;
    }
    
    public String getResponseFormat() {
        return responseFormat;
    }
    
    public String getNegativePrompt() {
        return negativePrompt;
    }
    
    /**
     * Builder for ImageRequest.
     */
    public static class Builder {
        private String model;
        private String prompt;
        private Integer n = 1;
        private String size = "1024x1024";
        private String quality = "standard";
        private String style = "vivid";
        private String responseFormat = "url";
        private String negativePrompt;
        
        public Builder model(String model) {
            this.model = model;
            return this;
        }
        
        public Builder prompt(String prompt) {
            this.prompt = prompt;
            return this;
        }
        
        public Builder n(int n) {
            this.n = n;
            return this;
        }
        
        public Builder size(String size) {
            this.size = size;
            return this;
        }
        
        public Builder quality(String quality) {
            this.quality = quality;
            return this;
        }
        
        public Builder style(String style) {
            this.style = style;
            return this;
        }
        
        public Builder responseFormat(String responseFormat) {
            this.responseFormat = responseFormat;
            return this;
        }
        
        public Builder negativePrompt(String negativePrompt) {
            this.negativePrompt = negativePrompt;
            return this;
        }
        
        public ImageRequest build() {
            return new ImageRequest(this);
        }
    }
}

