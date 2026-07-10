package com.bidhutkarki.tictactoe.user.exception;

import org.springframework.http.HttpStatusCode;

public class AuthServiceException extends RuntimeException {

    private final HttpStatusCode status;

    public AuthServiceException(HttpStatusCode status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatusCode getStatus() {
        return status;
    }
}
