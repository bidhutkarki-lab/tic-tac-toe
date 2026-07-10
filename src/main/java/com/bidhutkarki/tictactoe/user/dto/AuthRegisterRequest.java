package com.bidhutkarki.tictactoe.user.dto;

public record AuthRegisterRequest(String email, String username, String password) {

    public static AuthRegisterRequest from(RegisterRequest request) {
        return new AuthRegisterRequest(request.email(), request.username(), request.password());
    }
}
