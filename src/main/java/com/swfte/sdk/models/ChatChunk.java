package com.swfte.sdk.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * A streaming chat completion chunk.
 */
public class ChatChunk {
    
    private String id;
    private String object;
    private long created;
    private String model;
    private List<StreamChoice> choices;
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getObject() {
        return object;
    }
    
    public void setObject(String object) {
        this.object = object;
    }
    
    public long getCreated() {
        return created;
    }
    
    public void setCreated(long created) {
        this.created = created;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public List<StreamChoice> getChoices() {
        return choices;
    }
    
    public void setChoices(List<StreamChoice> choices) {
        this.choices = choices;
    }
    
    /**
     * A streaming choice.
     */
    public static class StreamChoice {
        private int index;
        private Delta delta;
        private Message message;  // For wrapped non-streaming responses

        @JsonProperty("finish_reason")
        private String finishReason;

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public Delta getDelta() {
            return delta;
        }

        public void setDelta(Delta delta) {
            this.delta = delta;
        }

        public Message getMessage() {
            return message;
        }

        public void setMessage(Message message) {
            this.message = message;
        }

        public String getFinishReason() {
            return finishReason;
        }

        public void setFinishReason(String finishReason) {
            this.finishReason = finishReason;
        }
    }
    
    /**
     * Delta content in streaming response.
     */
    public static class Delta {
        private String role;
        private String content;
        
        public String getRole() {
            return role;
        }
        
        public void setRole(String role) {
            this.role = role;
        }
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
    }
}

