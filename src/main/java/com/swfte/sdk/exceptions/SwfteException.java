package com.swfte.sdk.exceptions;

/**
 * Base exception for Swfte SDK errors.
 */
public class SwfteException extends RuntimeException {
    
    public SwfteException(String message) {
        super(message);
    }
    
    public SwfteException(String message, Throwable cause) {
        super(message, cause);
    }
}

