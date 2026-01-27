package com.swfte.sdk.resources;

import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.HttpClient;
import com.swfte.sdk.models.ChatRequest;
import com.swfte.sdk.models.ChatResponse;
import com.swfte.sdk.models.ChatChunk;

import java.util.stream.Stream;

/**
 * Chat API resource.
 */
public class Chat {
    
    private final Completions completions;
    
    public Chat(SwfteClient client) {
        this.completions = new Completions(client);
    }
    
    /**
     * Access the completions API.
     */
    public Completions completions() {
        return completions;
    }
    
    /**
     * Chat completions resource.
     */
    public static class Completions {
        
        private final HttpClient httpClient;
        
        public Completions(SwfteClient client) {
            this.httpClient = new HttpClient(client);
        }
        
        /**
         * Create a chat completion.
         *
         * @param request the chat request
         * @return chat response
         */
        public ChatResponse create(ChatRequest request) {
            return httpClient.post("/chat/completions", request, ChatResponse.class);
        }
        
        /**
         * Create a streaming chat completion.
         *
         * @param request the chat request (stream should be true)
         * @return stream of chat chunks
         */
        public Stream<ChatChunk> createStream(ChatRequest request) {
            ChatRequest streamRequest = ChatRequest.builder()
                .model(request.getModel())
                .messages(request.getMessages())
                .temperature(request.getTemperature() != null ? request.getTemperature() : 1.0)
                .stream(true)
                .build();
            
            // Use the dedicated streaming endpoint
            return httpClient.postStream("/chat/completions/stream", streamRequest)
                .map(json -> {
                    try {
                        return httpClient.getObjectMapper().readValue(json, ChatChunk.class);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(chunk -> chunk != null);
        }
    }
}

