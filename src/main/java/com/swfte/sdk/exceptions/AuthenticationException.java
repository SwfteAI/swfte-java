package com.swfte.sdk.exceptions;

/**
 * Exception thrown when authentication fails.
 */
public class AuthenticationException extends SwfteException {
    
    public AuthenticationException(String message) {
        super(message);
    }
}

