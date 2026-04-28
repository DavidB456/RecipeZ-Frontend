package com.recipez.core;

/**
 * Thrown by ApiClient when a request fails.
 * statusCode == 0 means a network/parse failure; otherwise it's the HTTP status.
 */
public class ApiException extends Exception {

    private final int statusCode;

    public ApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public ApiException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() { return statusCode; }

    public boolean isUnauthorized()  { return statusCode == 401; }
    public boolean isNotFound()      { return statusCode == 404; }
    public boolean isRateLimited()   { return statusCode == 429; }
    public boolean isNetworkError()  { return statusCode == 0; }

    /** Friendly message for showing to the user in dialogs. */
    public String userMessage() {
        if (isNetworkError())  return "Couldn't reach the server. Is the backend running?";
        if (isUnauthorized())  return "Invalid username or password.";
        if (isRateLimited())   return "Too many attempts. Please wait a moment and try again.";
        if (isNotFound())      return "Not found.";
        return "Server error (" + statusCode + ").";
    }
}
