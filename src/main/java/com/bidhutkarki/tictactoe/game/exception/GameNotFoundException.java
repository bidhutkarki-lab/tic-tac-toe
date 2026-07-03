package com.bidhutkarki.tictactoe.game.exception;

public class GameNotFoundException extends RuntimeException {

    public GameNotFoundException(Long id) {
        super("game '" + id + "' was not found");
    }
}
