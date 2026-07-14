package com.bidhutkarki.tictactoe.player.exception;

public class PlayerNotFoundException extends RuntimeException {

    public PlayerNotFoundException(String message) {
        super(message);
    }
}
