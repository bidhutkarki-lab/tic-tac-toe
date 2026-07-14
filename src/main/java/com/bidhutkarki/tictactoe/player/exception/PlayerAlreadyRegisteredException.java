package com.bidhutkarki.tictactoe.player.exception;

public class PlayerAlreadyRegisteredException extends RuntimeException {

    public PlayerAlreadyRegisteredException(String authId) {
        super("user '" + authId + "' already has a registered player");
    }
}
