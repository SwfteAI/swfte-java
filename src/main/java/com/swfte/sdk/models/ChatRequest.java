package com.swfte.sdk.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Request for chat completion.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatRequest {
    
    private String model;
    private List<Message> messages;
    private Double temperature;
    
    @JsonProperty("top_p")
    private Double topP;
    
    @JsonProperty("max_tokens")
    private Integer maxTokens;
    
    private Boolean stream;
    private Object stop;
    
    @JsonProperty("presence_penalty")
    private Double presencePenalty;
    
    @JsonProperty("frequency_penalty")
    private Double frequencyPenalty;
    
    private String user;
    
    private ChatRequest(Builder builder) {
        this.model = builder.model;
        this.messages = builder.messages;
        this.temperature = builder.temperature;
        this.topP = builder.topP;
        this.maxTokens = builder.maxTokens;
        this.stream = builder.stream;
        this.stop = builder.stop;
        this.presencePenalty = builder.presencePenalty;
        this.frequencyPenalty = builder.frequencyPenalty;
        this.user = builder.user;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Getters
    public String getModel() {
        return model;
    }
    
    public List<Message> getMessages() {
        return messages;
    }
    
    public Double getTemperature() {
        return temperature;
    }
    
    public Double getTopP() {
        return topP;
    }
    
    public Integer getMaxTokens() {
        return maxTokens;
    }
    
    public Boolean getStream() {
        return stream;
    }
    
    public Object getStop() {
        return stop;
    }
    
    public Double getPresencePenalty() {
        return presencePenalty;
    }
    
    public Double getFrequencyPenalty() {
        return frequencyPenalty;
    }
    
    public String getUser() {
        return user;
    }
    
    /**
     * Builder for ChatRequest.
     */
    public static class Builder {
        private String model;
        private List<Message> messages = new ArrayList<>();
        private Double temperature;
        private Double topP;
        private Integer maxTokens;
        private Boolean stream;
        private Object stop;
        private Double presencePenalty;
        private Double frequencyPenalty;
        private String user;
        
        public Builder model(String model) {
            this.model = model;
            return this;
        }
        
        public Builder messages(List<Message> messages) {
            this.messages = messages;
            return this;
        }
        
        public Builder addMessage(Message message) {
            this.messages.add(message);
            return this;
        }
        
        public Builder addMessage(String role, String content) {
            this.messages.add(new Message(role, content));
            return this;
        }
        
        public Builder temperature(double temperature) {
            this.temperature = temperature;
            return this;
        }
        
        public Builder topP(double topP) {
            this.topP = topP;
            return this;
        }
        
        public Builder maxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }
        
        public Builder stream(boolean stream) {
            this.stream = stream;
            return this;
        }
        
        public Builder stop(String stop) {
            this.stop = stop;
            return this;
        }
        
        public Builder stop(List<String> stop) {
            this.stop = stop;
            return this;
        }
        
        public Builder presencePenalty(double presencePenalty) {
            this.presencePenalty = presencePenalty;
            return this;
        }
        
        public Builder frequencyPenalty(double frequencyPenalty) {
            this.frequencyPenalty = frequencyPenalty;
            return this;
        }
        
        public Builder user(String user) {
            this.user = user;
            return this;
        }
        
        public ChatRequest build() {
            return new ChatRequest(this);
        }
    }
}

