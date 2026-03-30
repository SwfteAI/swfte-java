package com.swfte.sdk;

import com.swfte.sdk.exceptions.SwfteException;
import com.swfte.sdk.resources.Chat;
import com.swfte.sdk.resources.Images;
import com.swfte.sdk.resources.Embeddings;
import com.swfte.sdk.resources.Audio;
import com.swfte.sdk.resources.Models;
import com.swfte.sdk.resources.Agents;
import com.swfte.sdk.resources.Deployments;
import com.swfte.sdk.resources.Workflows;
import com.swfte.sdk.resources.Secrets;
import com.swfte.sdk.resources.Conversations;

/**
 * Swfte API client for accessing AI models through the unified gateway.
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * SwfteClient client = SwfteClient.builder()
 *     .apiKey("sk-swfte-...")
 *     .build();
 * 
 * ChatResponse response = client.chat().completions().create(
 *     ChatRequest.builder()
 *         .model("openai:gpt-4")
 *         .messages(List.of(new Message("user", "Hello!")))
 *         .build()
 * );
 * 
 * System.out.println(response.getChoices().get(0).getMessage().getContent());
 * }</pre>
 */
public class SwfteClient {
    
    private final String apiKey;
    private final String baseUrl;
    private final int timeout;
    private final int maxRetries;
    private final String workspaceId;
    
    private final Chat chat;
    private final Images images;
    private final Embeddings embeddings;
    private final Audio audio;
    private final Models models;
    private final Agents agents;
    private final Deployments deployments;
    private final Workflows workflows;
    private final Secrets secrets;
    private final Conversations conversations;
    
    private SwfteClient(Builder builder) {
        this.apiKey = builder.apiKey;
        this.baseUrl = builder.baseUrl;
        this.timeout = builder.timeout;
        this.maxRetries = builder.maxRetries;
        this.workspaceId = builder.workspaceId;
        
        // Initialize resources
        this.chat = new Chat(this);
        this.images = new Images(this);
        this.embeddings = new Embeddings(this);
        this.audio = new Audio(this);
        this.models = new Models(this);
        this.agents = new Agents(this);
        this.deployments = new Deployments(this);
        this.workflows = new Workflows(this);
        this.secrets = new Secrets(this);
        this.conversations = new Conversations(this);
    }
    
    /**
     * Create a new builder for SwfteClient.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Access the Chat API.
     */
    public Chat chat() {
        return chat;
    }
    
    /**
     * Access the Images API.
     */
    public Images images() {
        return images;
    }
    
    /**
     * Access the Embeddings API.
     */
    public Embeddings embeddings() {
        return embeddings;
    }
    
    /**
     * Access the Audio API.
     */
    public Audio audio() {
        return audio;
    }
    
    /**
     * Access the Models API.
     */
    public Models models() {
        return models;
    }
    
    /**
     * Access the Agents API.
     */
    public Agents agents() {
        return agents;
    }
    
    /**
     * Access the Deployments API (RunPod).
     */
    public Deployments deployments() {
        return deployments;
    }
    
    /**
     * Access the Workflows API.
     */
    public Workflows workflows() {
        return workflows;
    }

    /**
     * Access the Secrets API.
     */
    public Secrets secrets() {
        return secrets;
    }

    /**
     * Access the Conversations API.
     */
    public Conversations conversations() {
        return conversations;
    }

    public String getApiKey() {
        return apiKey;
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public int getTimeout() {
        return timeout;
    }
    
    public int getMaxRetries() {
        return maxRetries;
    }
    
    public String getWorkspaceId() {
        return workspaceId;
    }
    
    /**
     * Builder for SwfteClient.
     */
    public static class Builder {
        private String apiKey;
        private String baseUrl = "https://api.swfte.com/v2/gateway";
        private int timeout = 60000;
        private int maxRetries = 3;
        private String workspaceId;
        
        /**
         * Set the API key (required).
         */
        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }
        
        /**
         * Set the base URL for the API.
         */
        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl.replaceAll("/$", "");
            return this;
        }
        
        /**
         * Set the request timeout in milliseconds.
         */
        public Builder timeout(int timeout) {
            this.timeout = timeout;
            return this;
        }
        
        /**
         * Set the maximum number of retries.
         */
        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }
        
        /**
         * Set the workspace ID.
         */
        public Builder workspaceId(String workspaceId) {
            this.workspaceId = workspaceId;
            return this;
        }
        
        /**
         * Build the SwfteClient.
         */
        public SwfteClient build() {
            if (apiKey == null || apiKey.isEmpty()) {
                String envKey = System.getenv("SWFTE_API_KEY");
                if (envKey != null && !envKey.isEmpty()) {
                    apiKey = envKey;
                } else {
                    throw new SwfteException("API key is required. Set apiKey or SWFTE_API_KEY environment variable.");
                }
            }
            
            if (workspaceId == null || workspaceId.isEmpty()) {
                String envWorkspace = System.getenv("SWFTE_WORKSPACE_ID");
                if (envWorkspace != null && !envWorkspace.isEmpty()) {
                    workspaceId = envWorkspace;
                }
            }
            
            return new SwfteClient(this);
        }
    }
}

