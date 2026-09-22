package com.swfte.sdk.exceptions;

/**
 * Exception thrown when the API returns an error.
 */
public class ApiException extends SwfteException {
    private final int statusCode;
    private final String responseBody;

    public ApiException(String message, int statusCode) {
        this(message, statusCode, null);
    }

    public ApiException(String message, int statusCode, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    /** The raw error body returned by the server, when available. */
    public String getResponseBody() {
        return responseBody;
    }
}
