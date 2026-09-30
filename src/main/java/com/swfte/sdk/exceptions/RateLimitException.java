package com.swfte.sdk.exceptions;

/**
 * Exception thrown when rate limit is exceeded (HTTP 429). Never retried by the SDK.
 */
public class RateLimitException extends SwfteException {

    private final Long retryAfterSeconds;

    public RateLimitException(String message) {
        this(message, null);
    }

    public RateLimitException(String message, Long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    /** Seconds from the server's {@code Retry-After} header, or {@code null} when absent. */
    public Long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
