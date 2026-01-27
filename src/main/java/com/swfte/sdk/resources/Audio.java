package com.swfte.sdk.resources;

import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.HttpClient;
import com.swfte.sdk.models.TranscriptionResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Audio API resource.
 */
public class Audio {
    
    private final Transcriptions transcriptions;
    private final Speech speech;
    
    public Audio(SwfteClient client) {
        this.transcriptions = new Transcriptions(client);
        this.speech = new Speech(client);
    }
    
    /**
     * Access the transcriptions API.
     */
    public Transcriptions transcriptions() {
        return transcriptions;
    }
    
    /**
     * Access the speech API.
     */
    public Speech speech() {
        return speech;
    }
    
    /**
     * Audio transcription resource.
     */
    public static class Transcriptions {
        
        private final HttpClient httpClient;
        
        public Transcriptions(SwfteClient client) {
            this.httpClient = new HttpClient(client);
        }
        
        /**
         * Transcribe audio to text.
         *
         * @param model the model ID
         * @param audioData the audio file bytes
         * @return transcription response
         */
        public TranscriptionResponse create(String model, byte[] audioData) {
            return create(model, audioData, null, null);
        }
        
        /**
         * Transcribe audio to text.
         *
         * @param model the model ID
         * @param audioData the audio file bytes
         * @param language optional language code
         * @param prompt optional prompt
         * @return transcription response
         */
        public TranscriptionResponse create(String model, byte[] audioData, String language, String prompt) {
            Map<String, Object> fields = new HashMap<>();
            fields.put("model", model);
            fields.put("file", audioData);
            
            if (language != null) {
                fields.put("language", language);
            }
            if (prompt != null) {
                fields.put("prompt", prompt);
            }
            
            return httpClient.postMultipart("/audio/transcriptions", fields, TranscriptionResponse.class);
        }
    }
    
    /**
     * Text-to-speech resource.
     */
    public static class Speech {
        
        private final HttpClient httpClient;
        
        public Speech(SwfteClient client) {
            this.httpClient = new HttpClient(client);
        }
        
        /**
         * Generate speech from text.
         *
         * @param model the model ID
         * @param input the text to convert
         * @param voice the voice to use
         * @return audio bytes
         */
        public byte[] create(String model, String input, String voice) {
            Map<String, Object> request = new HashMap<>();
            request.put("model", model);
            request.put("input", input);
            request.put("voice", voice);
            
            return httpClient.postBytes("/audio/speech", request);
        }
        
        /**
         * Generate speech from text with format.
         *
         * @param model the model ID
         * @param input the text to convert
         * @param voice the voice to use
         * @param responseFormat the audio format
         * @param speed the speech speed
         * @return audio bytes
         */
        public byte[] create(String model, String input, String voice, String responseFormat, double speed) {
            Map<String, Object> request = new HashMap<>();
            request.put("model", model);
            request.put("input", input);
            request.put("voice", voice);
            request.put("response_format", responseFormat);
            request.put("speed", speed);
            
            return httpClient.postBytes("/audio/speech", request);
        }
    }
}

