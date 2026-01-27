package com.swfte.sdk.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response from image generation.
 */
public class ImageResponse {
    
    private long created;
    private List<ImageData> data;
    
    public long getCreated() {
        return created;
    }
    
    public void setCreated(long created) {
        this.created = created;
    }
    
    public List<ImageData> getData() {
        return data;
    }
    
    public void setData(List<ImageData> data) {
        this.data = data;
    }
    
    /**
     * Image data.
     */
    public static class ImageData {
        private String url;
        
        @JsonProperty("b64_json")
        private String b64Json;
        
        @JsonProperty("revised_prompt")
        private String revisedPrompt;
        
        public String getUrl() {
            return url;
        }
        
        public void setUrl(String url) {
            this.url = url;
        }
        
        public String getB64Json() {
            return b64Json;
        }
        
        public void setB64Json(String b64Json) {
            this.b64Json = b64Json;
        }
        
        public String getRevisedPrompt() {
            return revisedPrompt;
        }
        
        public void setRevisedPrompt(String revisedPrompt) {
            this.revisedPrompt = revisedPrompt;
        }
    }
}

