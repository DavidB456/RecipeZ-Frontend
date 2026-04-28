package com.recipez.core;

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

    public String userMessage() {
        if (isNetworkError())  return "Couldn't reach the server. Is the backend running?";
        if (isUnauthorized())  return "Invalid username or password.";
        if (isRateLimited())   return "Too many attempts. Please wait a moment and try again.";
        if (isNotFound())      return "Not found.";
        return "Server error (" + statusCode + ").";
    }
}
