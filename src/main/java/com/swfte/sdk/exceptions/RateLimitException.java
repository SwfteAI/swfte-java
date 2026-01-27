package com.swfte.sdk.exceptions;

/**
 * Exception thrown when rate limit is exceeded.
 */
public class RateLimitException extends SwfteException {
    
    public RateLimitException(String message) {
        super(message);
    }
}

