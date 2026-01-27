package com.swfte.sdk.models;

/**
 * Response from audio transcription.
 */
public class TranscriptionResponse {
    
    private String text;
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
}

