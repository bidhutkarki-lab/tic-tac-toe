package com.bidhutkarki.tictactoe.user.exception;

public class AuthServiceUnavailableException extends RuntimeException {

    public AuthServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthServiceUnavailableException(String message) {
        super(message);
    }
}
