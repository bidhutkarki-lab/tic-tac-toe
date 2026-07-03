package com.bidhutkarki.tictactoe.player.exception;

public class UsernameAlreadyExistsException extends RuntimeException {

    public UsernameAlreadyExistsException(String username) {
        super("username '" + username + "' is already taken");
    }
}
