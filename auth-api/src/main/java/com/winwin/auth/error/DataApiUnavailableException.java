package com.winwin.auth.error;

public class DataApiUnavailableException extends RuntimeException {

    public DataApiUnavailableException() {
        super("Transformation service is unavailable");
    }

    public DataApiUnavailableException(Throwable cause) {
        super("Transformation service is unavailable", cause);
    }
}
