package com.swfte.sdk.exceptions;

/**
 * Exception thrown when the API returns an error.
 */
public class ApiException extends SwfteException {
    
    private final int statusCode;
    
    public ApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
}

