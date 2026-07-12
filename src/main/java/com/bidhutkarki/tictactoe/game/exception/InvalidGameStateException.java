package com.bidhutkarki.tictactoe.game.exception;

public class InvalidGameStateException extends RuntimeException {

    public InvalidGameStateException(String message) {
        super(message);
    }
}
